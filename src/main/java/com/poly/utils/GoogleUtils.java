package com.poly.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.poly.dto.UserGoogleDto;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class GoogleUtils {

    // Khai báo biến static (không gán giá trị cứng nữa)
    public static final String GOOGLE_CLIENT_ID;
    public static final String GOOGLE_CLIENT_SECRET;
    public static final String GOOGLE_REDIRECT_URI;

    public static final String GOOGLE_GRANT_TYPE = "authorization_code";
    public static final String GOOGLE_LINK_GET_TOKEN = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";

    // Khối tĩnh (static block) - Chạy ngay khi class này được gọi để nạp dữ liệu từ file
    static {
        Properties props = new Properties();
        try (InputStream input = GoogleUtils.class.getClassLoader().getResourceAsStream("env.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find env.properties");
                // Giá trị mặc định nếu không tìm thấy file (tránh lỗi NullPointer)
                GOOGLE_CLIENT_ID = "";
                GOOGLE_CLIENT_SECRET = "";
                GOOGLE_REDIRECT_URI = "";
            } else {
                props.load(input);
                GOOGLE_CLIENT_ID = props.getProperty("GOOGLE_CLIENT_ID");
                GOOGLE_CLIENT_SECRET = props.getProperty("GOOGLE_CLIENT_SECRET");
                GOOGLE_REDIRECT_URI = props.getProperty("GOOGLE_REDIRECT_URI");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error loading Google Config", ex);
        }
    }

    // Bước 1: Đổi Code lấy Token (Giữ nguyên logic, chỉ đổi cách dùng biến)
    public static String getToken(String code) throws IOException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", GOOGLE_CLIENT_ID);
        parameters.put("client_secret", GOOGLE_CLIENT_SECRET);
        parameters.put("redirect_uri", GOOGLE_REDIRECT_URI);
        parameters.put("code", code);
        parameters.put("grant_type", GOOGLE_GRANT_TYPE);

        String form = parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_LINK_GET_TOKEN))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jobj = new Gson().fromJson(response.body(), JsonObject.class);
        // Kiểm tra null để tránh lỗi nếu Google trả về error thay vì token
        if (jobj.has("access_token")) {
            return jobj.get("access_token").getAsString();
        } else {
            // Log ra lỗi nếu có để dễ debug
            System.err.println("Google Login Error: " + response.body());
            return null;
        }
    }

    // Bước 2: Lấy thông tin User (Giữ nguyên)
    public static UserGoogleDto getUserInfo(String accessToken) throws IOException, InterruptedException {
        if (accessToken == null) return null;

        String link = GOOGLE_LINK_GET_USER_INFO + accessToken;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Gson().fromJson(response.body(), UserGoogleDto.class);
    }
}