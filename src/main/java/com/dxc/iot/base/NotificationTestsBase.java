package com.dxc.iot.base;

import org.testng.annotations.BeforeSuite;
import com.dxc.iot.utils.ConfigReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationTestsBase extends BaseTest {

    @BeforeSuite
    public void seedThresholdSettings() {
        System.out.println("=== Seeding guaranteed-breach threshold settings ===");
        try {
            String baseUrl = ConfigReader.get("base.url");
            String backendUrl = System.getProperty("backend.url");
            if (backendUrl == null || backendUrl.isEmpty()) {
                if (baseUrl.contains("localhost:4200")) {
                    backendUrl = "http://localhost:8080";
                } else if (baseUrl.contains("localhost")) {
                    backendUrl = "http://localhost:8080";
                } else {
                    backendUrl = baseUrl.replace("frontend-route", "backend-route");
                    if (backendUrl.endsWith("/")) {
                        backendUrl = backendUrl.substring(0, backendUrl.length() - 1);
                    }
                }
            }
            System.out.println("Frontend URL: " + baseUrl);
            System.out.println("Backend URL: " + backendUrl);

            HttpClient client = HttpClient.newHttpClient();

            // 1. Log in to get JWT token
            String loginJson = "{\"email\":\"" + ConfigReader.get("test.email") + "\",\"password\":\"" + ConfigReader.get("test.password") + "\"}";
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(backendUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            if (loginResponse.statusCode() != 200) {
                throw new RuntimeException("Login failed with status: " + loginResponse.statusCode() + " Body: " + loginResponse.body());
            }

            Pattern pattern = Pattern.compile("\"auth_token\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(loginResponse.body());
            if (!matcher.find()) {
                throw new RuntimeException("auth_token not found in login response: " + loginResponse.body());
            }
            String token = matcher.group(1);
            System.out.println("Logged in successfully. Token acquired.");

            // 2. Fetch existing settings to clean up
            HttpRequest getSettingsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(backendUrl + "/api/settings"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> getSettingsResponse = client.send(getSettingsRequest, HttpResponse.BodyHandlers.ofString());
            String settingsBody = getSettingsResponse.body();

            Pattern uuidPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
            Matcher uuidMatcher = uuidPattern.matcher(settingsBody);
            while (uuidMatcher.find()) {
                String id = uuidMatcher.group(1);
                System.out.println("Deleting existing setting ID: " + id);
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create(backendUrl + "/api/settings/" + id))
                        .header("Authorization", "Bearer " + token)
                        .DELETE()
                        .build();
                client.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
            }

            // 3. Post new low-threshold settings to guarantee breach/alert creation
            String[] payloads = {
                "{\"type\":\"Traffic\",\"metric\":\"density\",\"thresholdValue\":1.0,\"alertType\":\"above\"}",
                "{\"type\":\"Air_Pollution\",\"metric\":\"co\",\"thresholdValue\":1.0,\"alertType\":\"above\"}",
                "{\"type\":\"Street_Light\",\"metric\":\"power\",\"thresholdValue\":1.0,\"alertType\":\"above\"}"
            };

            for (String payload : payloads) {
                System.out.println("Posting setting: " + payload);
                HttpRequest postRequest = HttpRequest.newBuilder()
                        .uri(URI.create(backendUrl + "/api/settings"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();
                HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
                if (postResponse.statusCode() != 201) {
                    System.out.println("WARNING: Failed to post setting: " + postResponse.statusCode() + " Body: " + postResponse.body());
                }
            }
            System.out.println("=== Seeding completed successfully ===");

        } catch (Exception e) {
            System.err.println("Failed to seed threshold settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
