package com.lec.packages.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class WeatherApiController {

    @Value("${weather.api.key}") // ✅ 환경 변수에서 OpenWeather API 키 가져오기
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ 날씨 정보를 가져오는 API
    @GetMapping("/api/weather")
    public ResponseEntity<String> getWeather(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        try {
            // 🌦️ OpenWeather API 호출
            String weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat +
                    "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";
            String weatherResponse = restTemplate.getForObject(weatherUrl, String.class);

            // 🌍 Nominatim API를 사용하여 한국어 지역명 가져오기
            String nominatimUrl = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat +
                    "&lon=" + lon + "&accept-language=ko";
            String locationResponse = restTemplate.getForObject(nominatimUrl, String.class);

            // ✅ JSON 파싱 (지역명 변환)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode weatherJson = objectMapper.readTree(weatherResponse);
            JsonNode locationJson = objectMapper.readTree(locationResponse);

            // 지역명 한국어로 설정
            String koreanRegion = locationJson.path("address").path("state").asText();
            if (koreanRegion.isEmpty()) {
                koreanRegion = locationJson.path("address").path("city").asText();
            }
            if (koreanRegion.isEmpty()) {
                koreanRegion = locationJson.path("address").path("town").asText();
            }
            if (koreanRegion.isEmpty()) {
                koreanRegion = "알 수 없음";
            }

            // ✅ 지역명을 한국어로 바꿔서 JSON 수정
            ((ObjectNode) weatherJson.path("city")).put("name", koreanRegion);

            return ResponseEntity.ok(weatherJson.toString());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("날씨 정보를 가져오는 중 오류 발생: " + e.getMessage());
        }
    }
}
