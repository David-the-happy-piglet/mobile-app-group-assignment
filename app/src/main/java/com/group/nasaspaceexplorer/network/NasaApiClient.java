package com.group.nasaspaceexplorer.network;

import android.os.Handler;
import android.os.Looper;

import com.group.nasaspaceexplorer.model.ApodData;
import com.group.nasaspaceexplorer.model.RoverPhoto;

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

    // Get a free key at https://api.nasa.gov/  (DEMO_KEY: 30 req/hr, 50 req/day per IP)
    private static final String API_KEY = "DEMO_KEY";
    private static final String BASE_URL = "https://api.nasa.gov";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // ── APOD ─────────────────────────────────────────────────────────────────

    public static void fetchApod(String date, ApiCallback<ApodData> callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "/planetary/apod?api_key=" + API_KEY;
                if (date != null && !date.isEmpty()) {
                    urlStr += "&date=" + date;
                }
                String response = makeGetRequest(urlStr);
                ApodData apod = parseApod(response);
                handler.post(() -> callback.onSuccess(apod));
            } catch (Exception e) {
                String msg = friendlyError(e);
                handler.post(() -> callback.onError(msg));
            }
        });
    }

    // ── Mars Rover Photos ─────────────────────────────────────────────────────

    public static void fetchRoverPhotos(String rover, String camera, int sol,
                                        ApiCallback<List<RoverPhoto>> callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "/mars-photos/api/v1/rovers/"
                        + rover.toLowerCase()
                        + "/photos?sol=" + sol
                        + "&page=1"
                        + "&api_key=" + API_KEY;
                if (camera != null && !camera.equalsIgnoreCase("ALL")) {
                    urlStr += "&camera=" + camera.toLowerCase();
                }
                String response = makeGetRequest(urlStr);
                List<RoverPhoto> photos = parseRoverPhotos(response);
                handler.post(() -> callback.onSuccess(photos));
            } catch (Exception e) {
                String msg = friendlyError(e);
                handler.post(() -> callback.onError(msg));
            }
        });
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private static String makeGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json");

        int code = connection.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            return sb.toString();
        } else {
            // Try to read error body for a better message
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

    private static List<RoverPhoto> parseRoverPhotos(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray photosArr = root.getJSONArray("photos");
        List<RoverPhoto> photos = new ArrayList<>();
        for (int i = 0; i < photosArr.length(); i++) {
            JSONObject obj = photosArr.getJSONObject(i);
            RoverPhoto photo = new RoverPhoto();
            photo.setId(obj.optInt("id", 0));
            photo.setSol(obj.optInt("sol", 0));
            photo.setImgSrc(obj.optString("img_src", ""));
            photo.setEarthDate(obj.optString("earth_date", ""));

            JSONObject cam = obj.optJSONObject("camera");
            if (cam != null) {
                photo.setCameraName(cam.optString("name", ""));
                photo.setCameraFullName(cam.optString("full_name", ""));
            }

            JSONObject rover = obj.optJSONObject("rover");
            if (rover != null) {
                photo.setRoverName(rover.optString("name", ""));
                photo.setRoverStatus(rover.optString("status", ""));
            }
            photos.add(photo);
        }
        return photos;
    }

    // ── Error helpers ─────────────────────────────────────────────────────────

    private static String friendlyError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "Unknown error occurred.";
        if (msg.contains("HTTP 429")) return "Rate limit reached. Please wait a minute and try again.";
        if (msg.contains("HTTP 403")) return "API key invalid or rate limit exceeded.";
        if (msg.contains("UnknownHost") || msg.contains("unable to resolve"))
            return "No internet connection. Check your network settings.";
        if (msg.contains("timeout") || msg.contains("timed out"))
            return "Request timed out. Check your connection and try again.";
        return msg;
    }
}
