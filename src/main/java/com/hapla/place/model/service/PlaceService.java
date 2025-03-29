package com.hapla.place.model.service;

import com.hapla.place.model.mapper.PlaceMapper;
import com.hapla.place.model.vo.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceMapper mapper;

    @Value("${google.api.key}")
    private String apiKey;

    private final String GOOGLE_PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/details/json";

    public Map<String, Object> getPlaceDetails(String placeId) {
        // 'fields' 파라미터에 필요한 데이터만 요청합니다.
        String url = String.format("%s?placeid=%s&key=%s&language=ko&fields=photos,name,price_level,rating,formatted_address,formatted_phone_number,review", GOOGLE_PLACES_API_URL, placeId, apiKey);

        // RestTemplate을 이용해 GET 요청을 보내고, JSON 형식의 응답을 받습니다.
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        // JSON 파싱 후 필요한 정보만 반환하는 로직
        return parsePlaceDetails(response);
    }

    private Map<String, Object> parsePlaceDetails(String jsonResponse) {
        try {
            // Jackson ObjectMapper를 이용하여 JSON을 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonResponse, Map.class);
        } catch (Exception e) {
            // 예외 처리 (필요에 따라 로깅 등 추가 가능)
            e.printStackTrace();
            return null;
        }
    }

    public int countStar(String placeId) {
        return mapper.countStar(placeId);
    }

    public int checkPlace(Place place) {
        return mapper.checkPlace(place);
    }

    public int insertPlace(Place place) {
        return mapper.insertPlace(place);
    }

    public int deletePlace(Place place) {
        return mapper.deletePlace(place);
    }
}
