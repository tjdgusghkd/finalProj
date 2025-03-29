package com.hapla.flights.model.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hapla.flights.model.mapper.FlightMapper;
import com.hapla.flights.model.vo.Airport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class FlightService {
	private final FlightMapper fMapper;
	
	public List<Airport> searchList(String query) {
		return fMapper.searchList(query);
	}

	public int countPlus(HashMap<String, String> iataMap) {
		return fMapper.countPlus(iataMap);
	}

}
