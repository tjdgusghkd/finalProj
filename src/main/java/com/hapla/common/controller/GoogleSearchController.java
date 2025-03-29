package com.hapla.common.controller;

import com.hapla.users.model.service.UsersService;
import com.hapla.users.model.vo.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes("loginUser")
public class GoogleSearchController {

    private final UsersService usersService;

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    @GetMapping("/main")
    public String mainPage(Model model) {
        Users loginUser = (Users) model.getAttribute("loginUser");
        if (loginUser == null) {
            model.addAttribute("set", "loginX");
            return "/main";
        }

        List<Map<String, Object>> placesList = fetchFavoritePlaces(loginUser.getUserNo());
        model.addAttribute("places", placesList);
        model.addAttribute("set", "즐겨찾기");
        return "/main";
    }

    @GetMapping("/search/{city}/{category}/{select}")
    public String searchPlaces(@PathVariable("city") String city, @PathVariable("category") String category, @PathVariable("select") String select, Model model) {
        try {
            List<Map<String, Object>> places = getPlacesFromApi(null, city, category, 20);
            model.addAttribute("places", places);
            model.addAttribute("apiKey", googleApiKey);
            model.addAttribute("set", select);
            return "/main";
        } catch (Exception e) {
            log.error("Error during searchPlaces", e);
            model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return null;
        }
    }

    @GetMapping("/searchAll/{city}")
    public String searchAllPlaces(@PathVariable("city") String city, Model model) {
        try {
            Map<String, Object> filteredResponse = getAllPlacesData(city);
            model.addAttribute("categoryResults", filteredResponse.get("categoryResults"));
            model.addAttribute("main_info", filteredResponse.get("main_info"));
            model.addAttribute("set", "전체");
            model.addAttribute("categories", List.of("tourist_attraction", "landmark", "lodging", "restaurant"));
            return "/main";
        } catch (Exception e) {
            log.error("Error during searchAllPlaces", e);
            model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return null;
        }
    }

    private List<Map<String, Object>> fetchFavoritePlaces(int userNo) {
        List<String> placeIds = usersService.selectPlaceId(userNo);
        List<Map<String, Object>> placesList = new ArrayList<>();

        for (String placeId : placeIds) {
            List<Map<String, Object>> placeDetails = getPlacesFromApi(placeId, null, null, 0);
            if (!placeDetails.isEmpty()) {
                placesList.add(placeDetails.getFirst());
            }
        }
        return placesList;
    }

    private List<Map<String, Object>> getPlacesFromApi(String placeId, String city, String category, int limit) {
        List<Map<String, Object>> places = new ArrayList<>();

        try {
            String url, response;
            JSONObject jsonResponse;

            if (placeId != null && !placeId.isEmpty()) {
                url = String.format("https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&language=ko&key=%s", placeId, googleApiKey);
                response = restTemplate.getForObject(url, String.class);

                if (response != null && !response.trim().isEmpty()) {
                    jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("result") && !jsonResponse.isNull("result")) {JSONObject result = jsonResponse.getJSONObject("result");
                        Map<String, Object> placeData = new HashMap<>();
                        placeData.put("place_id", placeId);
                        placeData.put("name", result.optString("name", "이름 없음"));
                        placeData.put("rating", result.optDouble("rating", 0.0));
                        placeData.put("reviews", result.optInt("user_ratings_total", 0));
                        placeData.put("photo_url", getPhotoUrl(result));
                        placeData.put("types", result.optJSONArray("types") != null && !result.optJSONArray("types").isEmpty() ? result.optJSONArray("types").get(0) : "기타");
                        places.add(placeData);
                    }
                }
            } else if (city != null && !city.isEmpty() && category != null && !category.isEmpty()) {
                url = String.format("https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s+%s&language=ko&key=%s", city, category, googleApiKey);
                response = restTemplate.getForObject(url, String.class);

                if (response != null && !response.trim().isEmpty()) {
                    jsonResponse = new JSONObject(response);
                    JSONArray results = jsonResponse.getJSONArray("results");
                    int maxResults = Math.min(results.length(), limit);
                    JSONArray limitedResults = new JSONArray();
                    for (int i = 0; i < maxResults; i++) {
                        limitedResults.put(results.get(i));
                    }

                    for (int i = 0; i < limitedResults.length(); i++) {
                        JSONObject placeJson = limitedResults.getJSONObject(i);
                        Map<String, Object> placeData = new HashMap<>();

                        placeData.put("place_id", placeJson.optString("place_id"));
                        placeData.put("name", placeJson.optString("name"));
                        placeData.put("rating", placeJson.has("rating") ? placeJson.optDouble("rating") : 0.0); // rating이 없는 경우 0.0으로 설정
                        placeData.put("reviews", placeJson.has("user_ratings_total") ? placeJson.optInt("user_ratings_total") : 0); // reviews 없는 경우 0 으로 설정
                        placeData.put("photo_url", getPhotoUrl(placeJson));
                        placeData.put("types", category);
                        places.add(placeData);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching places from API", e);
        }
        return places;
    }

    private Map<String, Object> getMainInfo(String city) {
        try {
            String mainUrl = String.format("https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&language=ko&key=%s", city, googleApiKey);
            String response = restTemplate.getForObject(mainUrl, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray mainResults = jsonResponse.getJSONArray("results");
            if (mainResults != null && !mainResults.isEmpty()) {
                JSONObject mainPlace = mainResults.getJSONObject(0);

                Map<String, Object> mainInfo = new HashMap<>();
                mainInfo.put("name", mainPlace.get("name"));
                String photoUrl = getPhotoUrl(mainPlace);
                mainInfo.put("photo_url", photoUrl);
                mainInfo.put("description", mainPlace.has("formatted_address") ? mainPlace.getString("formatted_address") : "설명 없음");
                mainInfo.put("place_id", mainPlace.get("place_id"));
                mainInfo.put("weather", getWeather((String) mainPlace.get("place_id")));
                return mainInfo;
            }
        } catch (Exception e) {
            log.error("Error fetching main info for city: {}", city, e);
        }
        return new HashMap<>();
    }

    private Map<String, Object> getWeather(String placeId) {
        try {
            String detailsUrl = String.format("https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&language=en&key=%s", placeId, googleApiKey);
            String response = restTemplate.getForObject(detailsUrl, String.class);
            JSONObject jsonResponse = new JSONObject(response).getJSONObject("result");
            String enCity = (String) jsonResponse.get("name");

            String url = "https://api.weatherapi.com/v1/current.json?key=b7639a3b840040e1abc73111250703&q=" + enCity + "&lang=ko";
            String weatherResponse = restTemplate.getForObject(url, String.class);
            return new JSONObject(weatherResponse).getJSONObject("current").toMap();
        } catch (Exception e) {
            log.error("Error getting city in English for city: {}", placeId, e);
        }
        return null;
    }

    private Map<String, Object> getAllPlacesData(String city) {
        Map<String, Object> filteredResponse = new HashMap<>();
        String[] categories = {"tourist_attraction", "landmark", "lodging", "restaurant"};
        Map<String, List<Map<String, Object>>> categoryResults = new HashMap<>();

        filteredResponse.put("main_info", getMainInfo(city));

        for (String category : categories) {
            List<Map<String, Object>> categoryPlaces = getPlacesFromApi(null, city, category, 4); // 각 카테고리마다 4개씩 데이터 가져오기
            categoryResults.put(category, categoryPlaces);
        }

        filteredResponse.put("categoryResults", categoryResults);
        return filteredResponse;
    }

    private String getPhotoUrl(JSONObject place) {
        JSONArray photos = place.getJSONArray("photos");
        if (photos != null && !photos.isEmpty()) {
            String photoReference = (String) photos.getJSONObject(0).get("photo_reference");
            return String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=%s&key=%s", photoReference, googleApiKey);
        }
        return "/img/시나모롤.jpg";
    }
}