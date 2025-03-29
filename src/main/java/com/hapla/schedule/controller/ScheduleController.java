package com.hapla.schedule.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hapla.schedule.model.service.ScheduleService;
import com.hapla.schedule.model.vo.Detail;
import com.hapla.schedule.model.vo.DetailMemo;
import com.hapla.schedule.model.vo.DetailPlace;
import com.hapla.schedule.model.vo.Trip;
import com.hapla.users.model.vo.Users;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {

	private final ScheduleService scheduleService;
	private SqlSession session;

	// 캘린더 페이지로 이동
	@GetMapping("/calendar")
	public String ScheduleCalendar() {
		return "/schedule/calendar";
	}

	// 일정 페이지로 이동 / DB 저장
	@GetMapping("/schedule")
	public String Schedule(@ModelAttribute Trip trip, HttpSession session, Model model) {

		Users user = (Users) session.getAttribute("loginUser");

		// Trip 객체 생성
		trip.setUserNo(user.getUserNo());

		// 1. 서비스 호출하여 DB 저장
		scheduleService.saveTrip(trip);
	
		// 날짜 범위 생성
		List<Date> dateRange = scheduleService.getDateRange(trip.getStartDate(), trip.getEndDate());
		model.addAttribute("dateRange", dateRange); // 날짜 범위 추가

		// 2. Trip이 저장된 후, 해당 tripNo를 사용하여 기본 Detail 일정 추가
		// scheduleService.saveDefault(trip.getTripNo(), trip.getStartDate());
//		System.out.println("trip : " + trip);
		model.addAttribute("trip", trip);
		return "/schedule/schedule";
	}

	// 일정 목록 페이지로 이동
	@GetMapping("/list")
	public String ScheduleList(@ModelAttribute Trip trip, HttpSession session, Model model) {

		Users user = (Users) session.getAttribute("loginUser");

		List<Trip> schedules = scheduleService.getMySchedule(user.getUserNo());

		model.addAttribute("trip", schedules);
		return "/schedule/list";
	}

	// 일정 내용 페이지로 이동
	@GetMapping("/detail/{tripNo}")
	public String ScheduleDetail(@PathVariable("tripNo") int tripNo, @ModelAttribute Detail detail, HttpSession session,
			Model model) {

		// tripNo를 이용해 여행 정보를 조회
		Trip trip = scheduleService.getTripNo(tripNo);

		// 여행 상세 일정 조회
		//scheduleService.getTripDetail(trip.getTripNo());
		List<Detail> detailList = scheduleService.getTripDetail(tripNo);
		
//		System.out.println("trip : " + trip);
		model.addAttribute("trip", trip); // 여행 정보 추가
		model.addAttribute("detailList", detailList); // 일정 정보 추가

		return "/schedule/detail";
	}
	
	// 일정 내용 수정
	@GetMapping("/edit/{tripNo}")
	public String editTrip(@PathVariable("tripNo") int tripNo, Model model) {
	    // trip 정보
		List<Trip> trips = scheduleService.getTripsByTripNo(tripNo);
	    // 첫 번째 Trip을 선택하거나, 목록 전체를 모델에 추가
	    Trip trip = trips.isEmpty() ? null : trips.get(0);
		
	    // trip에 속한 detail 리스트
	    List<Detail> detailList = scheduleService.getDetailsByTripNo(tripNo);

	    // 필요 시 memo, place도 가져오기
	    List<DetailMemo> memoMap = scheduleService.getMemosByTripNo(tripNo);
	    List<DetailPlace> placeMap = scheduleService.getPlacesByTripNo(tripNo);

	    // 날짜 범위 생성
  		List<Date> dateRange = scheduleService.getDateRange(trip.getStartDate(), trip.getEndDate());
  		model.addAttribute("dateRange", dateRange); // 날짜 범위 추가
	
		Map<String, List<String>> memoMapByDetailNo = new HashMap<>();
		for (DetailMemo memo : memoMap) {
		    String detailNoStr = String.valueOf(memo.getDetailNo());
		    memoMapByDetailNo.computeIfAbsent(detailNoStr, k -> new ArrayList<>()).add(memo.getContent());
		}
	
		Map<String, List<String>> placeMapByDetailNo = new HashMap<>();
		for (DetailPlace place : placeMap) {
		    String detailNoStr = String.valueOf(place.getDetailNo());
		    placeMapByDetailNo.computeIfAbsent(detailNoStr, k -> new ArrayList<>()).add(place.getPlaceId());
		}

	
	    model.addAttribute("trip", trip);
	    model.addAttribute("detailList", detailList);
	    model.addAttribute("memoMap", memoMapByDetailNo);
	    model.addAttribute("placeMap", placeMapByDetailNo);
	    model.addAttribute("trip", trip);
	    model.addAttribute("tripList", trips); // 필요한 경우 전체 목록 추가

	    return "/schedule/scheduleEdit";
	}
	
	@GetMapping("/delete/{tripNo}")
	public String deleteTrip(@PathVariable("tripNo") int tripNo) {
	    scheduleService.deleteTrip(tripNo);
	    return "redirect:/schedule/list";
	}

	
	
	
	
}