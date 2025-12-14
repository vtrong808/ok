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

// Class tiện ích dùng để giao tiếp với Google API
public class GoogleUtils {

    // Các biến hằng số (Thông tin cấu hình)
    public static final String GOOGLE_CLIENT_ID;
    public static final String GOOGLE_CLIENT_SECRET;
    public static final String GOOGLE_REDIRECT_URI;

    // Đường dẫn API của Google
    public static final String GOOGLE_GRANT_TYPE = "authorization_code";
    public static final String GOOGLE_LINK_GET_TOKEN = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";

    // Khối static: Chạy ngay khi ứng dụng khởi động để đọc file cấu hình env.properties
    static {
        Properties props = new Properties();
        try (InputStream input = GoogleUtils.class.getClassLoader().getResourceAsStream("env.properties")) {
            if (input == null) {
                System.out.println("LỖI: Không tìm thấy file env.properties.");
                GOOGLE_CLIENT_ID = "";
                GOOGLE_CLIENT_SECRET = "";
                GOOGLE_REDIRECT_URI = "";
            } else {
                props.load(input);
                // Đọc giá trị từ file ra biến
                GOOGLE_CLIENT_ID = props.getProperty("GOOGLE_CLIENT_ID");
                GOOGLE_CLIENT_SECRET = props.getProperty("GOOGLE_CLIENT_SECRET");
                GOOGLE_REDIRECT_URI = props.getProperty("GOOGLE_REDIRECT_URI");

                System.out.println("Đã load cấu hình Google thành công!");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Lỗi đọc file cấu hình", ex);
        }
    }

    // Bước 1: Gửi mã 'code' sang Google để đổi lấy 'accessToken'
    public static String getToken(String code) throws IOException, InterruptedException {
        // Chuẩn bị tham số để gửi đi
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", GOOGLE_CLIENT_ID);
        parameters.put("client_secret", GOOGLE_CLIENT_SECRET);
        parameters.put("redirect_uri", GOOGLE_REDIRECT_URI);
        parameters.put("code", code);
        parameters.put("grant_type", GOOGLE_GRANT_TYPE);

        // Chuyển tham số thành chuỗi dạng key=value&key=value...
        String form = parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        // Tạo Client http (giống như mở trình duyệt)
        HttpClient client = HttpClient.newHttpClient();
        // Tạo Request POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_LINK_GET_TOKEN))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        // Gửi đi và nhận phản hồi
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Phân tích kết quả JSON trả về để lấy access_token
        JsonObject jobj = new Gson().fromJson(response.body(), JsonObject.class);
        if (jobj.has("access_token")) {
            return jobj.get("access_token").getAsString();
        } else {
            System.err.println("Google trả về lỗi: " + response.body());
            return null;
        }
    }

    // Bước 2: Dùng 'accessToken' để lấy thông tin cá nhân User
    public static UserGoogleDto getUserInfo(String accessToken) throws IOException, InterruptedException {
        if (accessToken == null) return null;

        String link = GOOGLE_LINK_GET_USER_INFO + accessToken;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET() // Gửi request GET
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Chuyển JSON trả về thành đối tượng Java (UserGoogleDto)
        return new Gson().fromJson(response.body(), UserGoogleDto.class);
    }
}