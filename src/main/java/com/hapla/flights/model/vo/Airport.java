package com.hapla.flights.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    private String iataCode;
    private String engAirportName;
    private String korAirportName;
    private String engCountryName;
    private String korCountryName;
    private String engCityName;
    private String korCityName;
    private int viewCount;
    
}
