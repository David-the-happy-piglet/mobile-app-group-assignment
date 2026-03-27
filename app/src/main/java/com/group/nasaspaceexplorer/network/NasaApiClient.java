package com.group.nasaspaceexplorer.network;

import android.os.Handler;
import android.os.Looper;

import com.group.nasaspaceexplorer.model.ApodData;
import com.group.nasaspaceexplorer.model.EpicPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NasaApiClient {

    private static final String API_KEY  = "0UshwmRznbPFXKacRzXbVQeR4cm6nSkpflqXa7DO"; // 换成真实 key
    private static final String BASE_URL = "https://api.nasa.gov";
    // EPIC 托管在独立域名，不走 api.nasa.gov，不需要 key，不限速
    private static final String EPIC_BASE = "https://epic.gsfc.nasa.gov";

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // ── APOD ──────────────────────────────────────────────────────────────────

    public static void fetchApod(String date, ApiCallback<ApodData> callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "/planetary/apod?api_key=" + API_KEY;
                if (date != null && !date.isEmpty()) urlStr += "&date=" + date;
                String response = makeGetRequest(urlStr);
                ApodData apod = parseApod(response);
                handler.post(() -> callback.onSuccess(apod));
            } catch (Exception e) {
                handler.post(() -> callback.onError(friendlyError(e)));
            }
        });
    }

    // ── EPIC Earth Photos ─────────────────────────────────────────────────────

    /**
     * 按日期获取 EPIC 自然色地球照片列表。
     * date 格式 "YYYY-MM-DD"；传 null 则获取最新一天的照片。
     * 使用 epic.gsfc.nasa.gov，无需 API key，无速率限制。
     */
    public static void fetchEpicPhotos(String date, ApiCallback<List<EpicPhoto>> callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String urlStr;
                if (date != null && !date.isEmpty()) {
                    urlStr = EPIC_BASE + "/api/natural/date/" + date;
                } else {
                    urlStr = EPIC_BASE + "/api/natural";
                }
                String response = makeGetRequest(urlStr);
                List<EpicPhoto> photos = parseEpicPhotos(response);
                handler.post(() -> callback.onSuccess(photos));
            } catch (Exception e) {
                handler.post(() -> callback.onError(friendlyError(e)));
            }
        });
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private static String makeGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Accept", "application/json");

        int code = connection.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            connection.disconnect();
            return sb.toString();
        } else {
            InputStream errStream = connection.getErrorStream();
            String body = "";
            if (errStream != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(errStream, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                body = sb.toString();
            }
            connection.disconnect();
            throw new IOException("HTTP " + code + (body.isEmpty() ? "" : ": " + body));
        }
    }

    // ── JSON parsers ──────────────────────────────────────────────────────────

    private static ApodData parseApod(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        ApodData apod = new ApodData();
        apod.setTitle(obj.optString("title", ""));
        apod.setDate(obj.optString("date", ""));
        apod.setExplanation(obj.optString("explanation", ""));
        apod.setUrl(obj.optString("url", ""));
        apod.setHdUrl(obj.optString("hdurl", ""));
        apod.setMediaType(obj.optString("media_type", "image"));
        apod.setCopyright(obj.optString("copyright", "").trim());
        return apod;
    }

    private static List<EpicPhoto> parseEpicPhotos(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        List<EpicPhoto> photos = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            EpicPhoto photo = new EpicPhoto();
            photo.setIdentifier(obj.optString("identifier", ""));
            photo.setCaption(obj.optString("caption", ""));
            photo.setImage(obj.optString("image", ""));
            photo.setDate(obj.optString("date", ""));

            JSONObject coords = obj.optJSONObject("coords");
            if (coords != null) {
                JSONObject centroid = coords.optJSONObject("centroid_coordinates");
                if (centroid != null) {
                    photo.setLat(centroid.optDouble("lat", 0));
                    photo.setLon(centroid.optDouble("lon", 0));
                }
            }
            photos.add(photo);
        }
        return photos;
    }

    // ── Error helpers ─────────────────────────────────────────────────────────

    private static String friendlyError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "Unknown error occurred.";
        if (msg.contains("HTTP 404")) return "No photos found for this date. Try a different date.";
        if (msg.contains("HTTP 429")) return "Rate limit reached. Please wait a minute and try again.";
        if (msg.contains("HTTP 403")) return "API key invalid or rate limit exceeded.";
        if (msg.contains("UnknownHost") || msg.contains("unable to resolve"))
            return "No internet connection. Check your network settings.";
        if (msg.contains("timeout") || msg.contains("timed out"))
            return "Request timed out. Check your connection and try again.";
        return msg;
    }
}