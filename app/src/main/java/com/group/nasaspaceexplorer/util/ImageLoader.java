package com.group.nasaspaceexplorer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads images from URLs on a background thread using only the Android API.
 * Caches decoded Bitmaps in an LruCache to avoid redundant downloads.
 *
 * Handles two known quirks of the NASA APIs:
 *  1. Some older endpoints return http:// URLs — these are upgraded to https://.
 *  2. Some servers redirect HTTP → HTTPS; redirects across protocols are
 *     followed manually (HttpURLConnection only auto-follows same-protocol redirects).
 */
public class ImageLoader {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    // Use 1/8 of available heap for the bitmap cache
    private static final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(
            (int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    public static void load(String url, ImageView imageView) {
        if (url == null || url.isEmpty() || imageView == null) return;

        // Normalise to HTTPS before cache lookup so http/https variants share an entry
        String httpsUrl = forceHttps(url);

        Bitmap cached = cache.get(httpsUrl);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setTag(httpsUrl);
        imageView.setImageBitmap(null);

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Bitmap bitmap = downloadBitmap(httpsUrl);
            if (bitmap != null) {
                cache.put(httpsUrl, bitmap);
                handler.post(() -> {
                    if (httpsUrl.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    public interface LoadCallback {
        void onFinished(boolean success);
    }

    public static void load(String url, ImageView imageView, LoadCallback callback) {
        if (url == null || url.isEmpty() || imageView == null) {
            if (callback != null) callback.onFinished(false);
            return;
        }

        String httpsUrl = forceHttps(url);

        Bitmap cached = cache.get(httpsUrl);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            if (callback != null) callback.onFinished(true);
            return;
        }

        imageView.setTag(httpsUrl);
        imageView.setImageBitmap(null);

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Bitmap bitmap = downloadBitmap(httpsUrl);
            if (bitmap != null) {
                cache.put(httpsUrl, bitmap);
                handler.post(() -> {
                    if (httpsUrl.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                    if (callback != null) callback.onFinished(true);
                });
            } else {
                handler.post(() -> {
                    if (callback != null) callback.onFinished(false);
                });
            }
        });
    }

    // ── Network ───────────────────────────────────────────────────────────────

    private static Bitmap downloadBitmap(String urlString) {
        return downloadBitmap(urlString, 0);
    }

    /**
     * Downloads and decodes a Bitmap. Follows up to 3 redirects (including
     * protocol-changing ones that HttpURLConnection won't auto-follow).
     */
    private static Bitmap downloadBitmap(String urlString, int redirectCount) {
        if (redirectCount > 3) return null;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(forceHttps(urlString));
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setInstanceFollowRedirects(true); // handles same-protocol redirects
            conn.connect();

            int code = conn.getResponseCode();

            // Manually follow protocol-changing redirects (e.g. http → https)
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location != null && !location.isEmpty()) {
                    return downloadBitmap(location, redirectCount + 1);
                }
                return null;
            }

            if (code != HttpURLConnection.HTTP_OK) return null;

            // Read the full response into memory so we can do a two-pass decode
            // (first pass gets dimensions; second pass subsamples to save RAM).
            byte[] data = readBytes(conn.getInputStream());

            // First pass: dimensions only
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);

            // Second pass: subsample so the decoded image fits in ~1024 × 1024
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, 1024, 1024);

            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);

        } catch (Exception ignored) {
            // Connection failure, SSL error, OOM during decode, etc.
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Upgrades an http:// URL to https://. Leaves https:// URLs unchanged. */
    private static String forceHttps(String url) {
        if (url != null && url.startsWith("http://")) {
            return "https://" + url.substring(7);
        }
        return url;
    }

    private static byte[] readBytes(InputStream input) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = input.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        input.close();
        return buffer.toByteArray();
    }

    /**
     * Returns the largest power-of-two sample size such that the decoded
     * image is at least {@code reqW} × {@code reqH} pixels.
     */
    private static int calculateSampleSize(int rawW, int rawH, int reqW, int reqH) {
        int sample = 1;
        if (rawH > reqH || rawW > reqW) {
            int halfH = rawH / 2;
            int halfW = rawW / 2;
            while (halfH / sample >= reqH && halfW / sample >= reqW) {
                sample *= 2;
            }
        }
        return sample;
    }
}
