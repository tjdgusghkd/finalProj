package com.hapla.flights.model.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hapla.flights.model.vo.Airport;

@Mapper
public interface FlightMapper {

	List<Airport> searchList(String query);

	int countPlus(HashMap<String, String> iataMap);

}
