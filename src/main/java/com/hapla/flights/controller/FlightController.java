package com.hapla.flights.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.hapla.flights.model.service.FlightService;
import com.hapla.flights.model.vo.AirlineInfo;
import com.hapla.flights.model.vo.Airport;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/flight")
public class FlightController {

    @Value("${AMADEUS.API.ID}")
    private String AMADEUS_API_ID;
    @Value("${AMADEUS.API.KEY}")
    private String AMADEUS_API_KEY;
    @Value("${TAGO.API.KEY}")
    private String TAGO_API_KEY;

    // 조회해온 항공편 데이터에서 항공사 명을 한글로 띄워주기 위한 CSV 파일 로드
    private final FlightService fService;

    public List<AirlineInfo> loadCSV() {
        List<AirlineInfo> airlineList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("src/main/resources/static/csv/airlineNames.csv"))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                AirlineInfo airline = new AirlineInfo();
                airline.setKorAirline(values[1]);
                airline.setCarrierCode(values[2]);

                airlineList.add(airline);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return airlineList;
    }

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/EUR";

    public double getExchangeRate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // API에서 데이터를 가져오기
            String response = restTemplate.getForObject(API_URL, String.class);
            JSONObject json = new JSONObject(response);
            JSONObject rates = json.getJSONObject("rates");
            return rates.getDouble("KRW");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error while fetching exchange rates", e);
        }
    }

    @GetMapping(value = "/search", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public List<Airport> searchAirports(@RequestParam("query") String query) {
        List<Airport> searchList = fService.searchList(query);
        return searchList;
    }

    public boolean isDomesticFlight(String departureCode, String arrivalCode) {
        List<String> koreanAirports = Arrays.asList("ICN", "GMP", "PUS", "CJU", "TAE", "KWJ", "RSU", "KPO", "WJU",
                "USN", "HIN", "YNY");
        return koreanAirports.contains(departureCode) && koreanAirports.contains(arrivalCode);
    }

    public String getAirportId(String iataCode) {
        Map<String, String> airportIdMap = new HashMap<>();
        airportIdMap.put("ICN", "NAARKSI");
        airportIdMap.put("GMP", "NAARKSS");
        airportIdMap.put("PUS", "NAARKPK");
        airportIdMap.put("CJU", "NAARKPC");
        airportIdMap.put("TAE", "NAARKTN");
        airportIdMap.put("KWJ", "NAARKJJ");
        airportIdMap.put("RSU", "NAARKJY");
        airportIdMap.put("KPO", "NAARKTH");
        airportIdMap.put("WJU", "NAARKNW");
        airportIdMap.put("USN", "NAARKPU");
        airportIdMap.put("HIN", "NAARKPS");
        airportIdMap.put("YNY", "NAARKNY");

        return airportIdMap.getOrDefault(iataCode, "UNKNOWN");
    }

    public String getAmadeusAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://test.api.amadeus.com/v1/security/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials&client_id=" + AMADEUS_API_ID + "&client_secret=" + AMADEUS_API_KEY;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(response.getBody());
                System.out.println("json : " + json);
                return json.getString("access_token");
            }
        } catch (Exception e) {
            System.out.println("Amadeus Token Request Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> getDomesticFlightOffers(String departure, String arrival, String departureDate,
                                                             String returnDate, String travelers) {
        List<Map<String, Object>> results = new ArrayList<>();
        HttpURLConnection conn = null;
        BufferedReader rd = null;

        try {
            if (departure == null || arrival == null || departureDate == null) {
                throw new IllegalArgumentException("필수 파라미터가 누락되었습니다.");
            }

            StringBuilder urlBuilder = new StringBuilder(
                    "http://apis.data.go.kr/1613000/DmstcFlightNvgInfoService/getFlightOpratInfoList");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + TAGO_API_KEY);
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("depAirportId", "UTF-8") + "="
                    + URLEncoder.encode(getAirportId(departure), "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("arrAirportId", "UTF-8") + "="
                    + URLEncoder.encode(getAirportId(arrival), "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("depPlandTime", "UTF-8") + "="
                    + URLEncoder.encode(departureDate.replace("-", ""), "UTF-8"));
            URL url = new URL(urlBuilder.toString());

            System.out.println("API URL: " + url);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            if (responseCode >= 200 && responseCode <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            String responseBody = sb.toString();
            System.out.println("API Response Body: " + responseBody);

            JSONObject json = new JSONObject(responseBody);
            JSONObject response = json.getJSONObject("response");
            JSONObject body = response.getJSONObject("body");
            JSONObject items = body.getJSONObject("items");
            JSONArray itemArray = items.getJSONArray("item");

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject flightJson = itemArray.getJSONObject(i);
                Map<String, Object> flightMap = new HashMap<>();
                System.out.println("json data:" + flightJson);
                LocalDateTime arrPlandTime = LocalDateTime
                        .parse(formatTAGODateTime(String.valueOf(flightJson.getLong("arrPlandTime"))));
                LocalDateTime depPlandTime = LocalDateTime
                        .parse(formatTAGODateTime(String.valueOf(flightJson.getLong("depPlandTime"))));
                flightMap.put("arrPlandTime", arrPlandTime);
                flightMap.put("airlineNm", flightJson.getString("airlineNm"));
                flightMap.put("arrAirportNm", flightJson.getString("arrAirportNm"));
                flightMap.put("depPlandTime", depPlandTime);
                flightMap.put("depAirportNm", flightJson.getString("depAirportNm"));
                flightMap.put("vihicleId", flightJson.getString("vihicleId"));

                // 필드가 없을 경우 기본값 0 설정
                flightMap.put("economyCharge", flightJson.optInt("economyCharge", 0));

                results.add(flightMap);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rd != null)
                    rd.close();
                if (conn != null)
                    conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Results: " + results);
        return results;
    }

    public String formatTAGODateTime(String tagoDateTime) {
        return tagoDateTime.substring(0, 4) + "-" + tagoDateTime.substring(4, 6) + "-" + tagoDateTime.substring(6, 8)
                + "T" + tagoDateTime.substring(8, 10) + ":" + tagoDateTime.substring(10, 12) + ":00";
    }

    @GetMapping("/flightSearch")
    public String flightSearch(@RequestParam("departureName") String departure,
                               @RequestParam("arrivalName") String arrival, @RequestParam("dates") String dates,
                               @RequestParam("travelers") String travelers, Model model) {

        System.out.println("=== Flight Search Started ===");
        System.out.println("Input parameters:");
        System.out.println("Departure: " + departure);
        System.out.println("Arrival: " + arrival);
        System.out.println("Dates: " + dates);
        System.out.println("Travelers: " + travelers);

        String iataPattern = "\\((\\w{3})\\)";
        Pattern pattern = Pattern.compile(iataPattern);
        Matcher departureMatcher = pattern.matcher(departure);
        Matcher arrivalMatcher = pattern.matcher(arrival);
        if (!departureMatcher.find() || !arrivalMatcher.find()) {
            System.out.println("Failed to extract IATA codes");
            model.addAttribute("error", "출발지와 도착지에서 IATA 코드를 찾을 수 없습니다.");
            return "flightSearchResult";
        }

        String departureCode = departureMatcher.group(1);
        String arrivalCode = arrivalMatcher.group(1);
        HashMap<String, String> iataMap = new HashMap<String, String>();
        iataMap.put("departureCode", departureCode);
        iataMap.put("arrivalCode", arrivalCode);

        try {
            System.out.println("Extracted IATA codes - Departure: " + departureCode + ", Arrival: " + arrivalCode);

            String[] dateSplit = dates.split(" ~ ");
            String departureDate = dateSplit[0].trim();
            String returnDate = (dateSplit.length > 1) ? dateSplit[1].trim() : null;

            System.out.println("Parsed dates - Departure: " + departureDate + ", Return: " + returnDate);

            // 1. departureDate 파싱 및 유효성 검사 (returnDate는 기본값으로 강제로 설정하지 않음)
            try {
                LocalDate depDate = LocalDate.parse(departureDate);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid departure date format: " + departureDate);

                model.addAttribute("error", "잘못된 날짜 형식입니다.");
                return "flightSearchResult";
            }

            // 2. returnDate가 제공된 경우에만 파싱 및 유효성 검사
            if (returnDate != null && !returnDate.trim().isEmpty()) {
                try {
                    LocalDate depDate = LocalDate.parse(departureDate);
                    LocalDate retDate = LocalDate.parse(returnDate);
                    if (retDate.isBefore(depDate)) {
                        System.out.println(
                                "Return date is before departure date: " + returnDate + ", treating as one-way.");
                        returnDate = null; // 출발일보다 이전이면 편도로 처리
                    } else if (retDate.isEqual(depDate)) {
                        System.out
                                .println("Return date equals departure date: " + returnDate + ", treating as one-way.");
                        returnDate = null; // 출발일과 같으면 편도로 처리 (필요에 따라 정책 변경 가능)
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid return date format: " + returnDate);

                    model.addAttribute("error", "잘못된 반환 날짜 형식입니다.");
                    return "flightSearchResult";

                }
            } else {
                System.out.println("No return date provided, treating as one-way search.");
            }

            boolean isDomestic = isDomesticFlight(departureCode, arrivalCode);
            System.out.println("Is domestic flight: " + isDomestic);

            System.out.println("API Keys present:");
            System.out.println("TAGO_API_KEY: " + TAGO_API_KEY);
            System.out.println(
                    "AMADEUS_API_ID: " + AMADEUS_API_ID + (AMADEUS_API_ID != null && !AMADEUS_API_ID.isEmpty()));
            System.out.println(
                    "AMADEUS_API_KEY: " + AMADEUS_API_KEY + (AMADEUS_API_KEY != null && !AMADEUS_API_KEY.isEmpty()));

            List<Map<String, Object>> flightOffers;
            if (isDomestic) {
                flightOffers = getDomesticFlightOffers(departureCode, arrivalCode, departureDate, returnDate,
                        travelers);
                System.out.println("flightOffers : " + flightOffers);
            } else {
                String accessToken = getAmadeusAccessToken();
                if (accessToken == null) {
                    System.out.println("Failed to get Amadeus access token");

                    model.addAttribute("error", "국제선 항공권 검색 실패: 인증 오류");
                    return "flightSearchResult";

                }
                flightOffers = getFlightOffers(accessToken, departureCode, arrivalCode, departureDate, returnDate,
                        travelers);
            }

            if (flightOffers == null || flightOffers.isEmpty()) {
                System.out.println("No flight offers retrieved.");

                model.addAttribute("error", "항공편을 찾을 수 없습니다.");
                return "flightSearchResult";

            }

            List<String> uniqueAirlines = flightOffers.stream().map(offer -> (String) offer.get("airline"))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());

            Set<String> uniqueFlightNumbers = new HashSet<>();
            List<Map<String, Object>> uniqueFlightOffers = new ArrayList<>();

            for (Map<String, Object> offer : flightOffers) {
                String flightNumber = "";
                if ((String) offer.get("flightNumber") != null) {
                    flightNumber = (String) offer.get("flightNumber");
                } else {
                    flightNumber = (String) offer.get("vihicleId");
                }
                if (!uniqueFlightNumbers.contains(flightNumber)) {
                    System.out.println("flightNumber : " + flightNumber);
                    System.out.println("offer : " + offer);
                    uniqueFlightNumbers.add(flightNumber);
                    uniqueFlightOffers.add(offer);
                }
            }

            System.out.println("<<<<<<<<<" + uniqueFlightOffers);
            System.out.println("Flight offers found: " + uniqueFlightOffers.size());
            if (!uniqueFlightOffers.isEmpty()) {
                System.out.println("First flight offer: " + uniqueFlightOffers.get(0));
                System.out.println("Outbound Airline: " + uniqueFlightOffers.get(0).get("outboundAirline"));
                System.out.println("x: " + uniqueFlightOffers.get(0).get("inboundAirline"));
            }
            fService.countPlus(iataMap);
            List<AirlineInfo> airlineList = loadCSV();
            model.addAttribute("flightOffers", uniqueFlightOffers).addAttribute("airline", airlineList);
            System.out.println("uniqueFlightOffers : " + uniqueFlightOffers);
            return "flightSearchResult";

        } catch (Exception e) {
            System.out.println("Exception in flight search: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "항공권 검색 중 오류가 발생했습니다: " + e.getMessage());
            return "flightSearchResult";

        }
    }

    public List<Map<String, Object>> getFlightOffers(String accessToken, String departure, String arrival,
                                                     String departureDate, String returnDate, String travelers) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://test.api.amadeus.com/v2/shopping/flight-offers" + "?originLocationCode=" + departure
                + "&destinationLocationCode=" + arrival + "&departureDate=" + departureDate
                + (returnDate != null ? "&returnDate=" + returnDate : "") + "&adults=" + travelers + "&max=200";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        System.out.println("url : " + url);
        HttpEntity<String> request = new HttpEntity<>(headers);
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            System.out.println("Amadeus API URL: " + url);
            System.out.println("Amadeus API Response: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(response.getBody());
                JSONArray flights = json.getJSONArray("data");
                JSONObject dictionaries = json.optJSONObject("dictionaries");
                JSONObject carriersDict = (dictionaries != null) ? dictionaries.optJSONObject("carriers")
                        : new JSONObject();

                System.out.println("Number of flights returned: " + flights.length());

                for (int i = 0; i < flights.length(); i++) {
                    JSONObject flight = flights.getJSONObject(i);
                    JSONArray itineraries = flight.getJSONArray("itineraries");

                    JSONObject outboundItinerary = itineraries.getJSONObject(0);
                    JSONArray outboundSegments = outboundItinerary.getJSONArray("segments");
                    JSONObject outboundFirstSegment = outboundSegments.getJSONObject(0);
                    JSONObject outboundLastSegment = outboundSegments.getJSONObject(outboundSegments.length() - 1);

                    JSONObject inboundItinerary = itineraries.optJSONObject(1);
                    JSONArray inboundSegments = null;
                    JSONObject inboundFirstSegment = null;
                    JSONObject inboundLastSegment = null;
                    if (inboundItinerary != null) {
                        inboundSegments = inboundItinerary.getJSONArray("segments");
                        inboundFirstSegment = inboundSegments.getJSONObject(0);
                        inboundLastSegment = inboundSegments.getJSONObject(inboundSegments.length() - 1);
                    }

                    Map<String, Object> flightData = new HashMap<>();
                    double exchangeRate = getExchangeRate();
                    double totalPrice = Double.parseDouble(flight.getJSONObject("price").getString("total"));
                    int numberOfPassengers = Integer.parseInt(travelers);
                    double pricePerPerson = totalPrice / numberOfPassengers; // 1인 기준 가격 계산
                    int result = (int) Math.ceil(exchangeRate * pricePerPerson);

                    flightData.put("price", result + "원");

                    // carrierCode 추가
                    String outboundCarrierCode = outboundFirstSegment.getString("carrierCode");
                    String outboundAirline = carriersDict.optString(outboundCarrierCode, outboundCarrierCode);
                    String outboundKorAirlineName = null;
                    String inboundKorAirlineName = null;
                    List<AirlineInfo> airlineList = loadCSV();
                    for (AirlineInfo airline : airlineList) {
                        if (airline.getCarrierCode().equals(outboundCarrierCode)) {
                            outboundKorAirlineName = airline.getKorAirline();
                        }
                    }
                    flightData.put("carrierCode", outboundCarrierCode); // ✅ 추가
                    flightData.put("airline", outboundAirline);
                    flightData.put("outboundAirline", outboundAirline);
                    flightData.put("outboundCarrierCode", outboundCarrierCode); // ✅ 추가
                    flightData.put("inboundSegment", inboundSegments);
                    flightData.put("outboundKorAirlineName", outboundKorAirlineName);

                    if (inboundFirstSegment != null) {
                        String inboundCarrierCode = inboundFirstSegment.getString("carrierCode");
                        String inboundAirline = carriersDict.optString(inboundCarrierCode, inboundCarrierCode);
                        for (AirlineInfo airline : airlineList) {
                            if (airline.getCarrierCode().equals(inboundCarrierCode)) {
                                inboundKorAirlineName = airline.getKorAirline();
                            }
                        }
                        flightData.put("inboundCarrierCode", inboundCarrierCode); // ✅ 추가
                        flightData.put("inboundAirline", inboundAirline);
                        flightData.put("inboundKorAirlineName", inboundKorAirlineName);
                    } else {
                        flightData.put("inboundAirline", outboundAirline);
                        flightData.put("inboundCarrierCode", outboundCarrierCode); // ✅ 추가
                    }

                    flightData.put("outboundDepartureTime",
                            LocalDateTime.parse(outboundFirstSegment.getJSONObject("departure").getString("at")));
                    flightData.put("outboundDepartureAirport",
                            outboundFirstSegment.getJSONObject("departure").getString("iataCode"));
                    flightData.put("outboundArrivalTime",
                            LocalDateTime.parse(outboundLastSegment.getJSONObject("arrival").getString("at")));
                    flightData.put("outboundArrivalAirport",
                            outboundLastSegment.getJSONObject("arrival").getString("iataCode"));
                    flightData.put("inboundSegment", inboundSegments);

                    if (inboundFirstSegment != null && inboundLastSegment != null) {
                        flightData.put("inboundDepartureTime",
                                LocalDateTime.parse(inboundFirstSegment.getJSONObject("departure").getString("at")));
                        flightData.put("inboundDepartureAirport",
                                inboundFirstSegment.getJSONObject("departure").getString("iataCode"));
                        flightData.put("inboundArrivalTime",
                                LocalDateTime.parse(inboundLastSegment.getJSONObject("arrival").getString("at")));
                        flightData.put("inboundArrivalAirport",
                                inboundLastSegment.getJSONObject("arrival").getString("iataCode"));
                    }

                    if (outboundSegments.length() > 1) {
                        flightData.put("outboundHasConnections", "true");
                        flightData.put("outboundTotalStops", String.valueOf(outboundSegments.length() - 1));

                        StringBuilder outboundConnections = new StringBuilder();
                        for (int j = 0; j < outboundSegments.length() - 1; j++) {
                            JSONObject segment = outboundSegments.getJSONObject(j);
                            outboundConnections.append(segment.getJSONObject("arrival").getString("iataCode"));
                            if (j < outboundSegments.length() - 2) {
                                outboundConnections.append(", ");
                            }
                        }
                        flightData.put("outboundConnectionAirports", outboundConnections.toString());
                    } else {
                        flightData.put("outboundHasConnections", "false");
                        flightData.put("outboundTotalStops", "0");
                    }

                    if (inboundSegments != null && inboundSegments.length() > 1) {
                        flightData.put("inboundHasConnections", "true");
                        flightData.put("inboundTotalStops", String.valueOf(inboundSegments.length() - 1));

                        StringBuilder inboundConnections = new StringBuilder();
                        for (int j = 0; j < inboundSegments.length() - 1; j++) {
                            JSONObject segment = inboundSegments.getJSONObject(j);
                            inboundConnections.append(segment.getJSONObject("arrival").getString("iataCode"));
                            if (j < inboundSegments.length() - 2) {
                                inboundConnections.append(", ");
                            }
                        }
                        flightData.put("inboundConnectionAirports", inboundConnections.toString());
                    } else if (inboundSegments != null) {
                        flightData.put("inboundHasConnections", "false");
                        flightData.put("inboundTotalStops", "0");
                    }

                    flightData.put("flightNumber", outboundFirstSegment.getString("number"));

                    results.add(flightData);
                }
            } else {
                System.out.println(
                        "Amadeus API Error: Status " + response.getStatusCode() + ", Body: " + response.getBody());
            }
        } catch (Exception e) {
            System.out.println("Amadeus API Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }
}