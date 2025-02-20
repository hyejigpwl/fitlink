package com.lec.packages.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lec.packages.domain.Facility;
import com.lec.packages.domain.Member;
import com.lec.packages.domain.Reservation;
import com.lec.packages.dto.FacilityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.ReservationDTO;
import com.lec.packages.dto.SalesDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.FacilityRepository;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.repository.ReservationRepository;
import com.lec.packages.security.CustomUserDetailsService;
import com.lec.packages.service.FacilityService;
import com.lec.packages.service.ReservationService;
import com.lec.packages.service.RevenueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping({ "/admin", "/api/admin" })
@RequiredArgsConstructor
public class AdminController {

	private final FacilityService facilityService;
	private final FacilityRepository facilityRepository;
	private final ReservationService reservationService;
	private final ReservationRepository reservationRepository;
	private final MemberRepository memberRepository;
	private final CustomUserDetailsService customUserDetailsService;
	private final RevenueService revenueService;

	@GetMapping("/main")
	public String adminMainPage(@AuthenticationPrincipal UserDetails userDetails, PageRequestDTO pageRequestDTO,
			Model model) {

		String userId = userDetails.getUsername();

		PageResponseDTO<FacilityDTO> responseFacilityDTO = facilityService.listByUser(userId, pageRequestDTO);
		PageResponseDTO<ReservationDTO> responseReservationDTO = reservationService.getAllReservationsForUser(userId,
				pageRequestDTO);

		// 매출데이터 가져오기
		List<SalesDTO> salesData = revenueService.getSalesData(userId);

		// 날짜 범위 생성(현재날짜와 지난 7일)
		LocalDate today = LocalDate.now();
		LocalDate sevenDaysAgo = today.minusDays(6);

		LocalDate lastWeekStart = today.minusWeeks(1).minusDays(6);
		LocalDate lastWeekEnd = today.minusWeeks(1);

		// 날짜 범위 생성(지난 5개월)
		LocalDate fiveMonthsAgo = today.minusMonths(4);

		List<String> dailyLabels = sevenDaysAgo.datesUntil(today.plusDays(1)).map(LocalDate::toString)
				.collect(Collectors.toList());

		List<String> lastWeekLabels = lastWeekStart.datesUntil(today.plusDays(1)).map(LocalDate::toString)
				.collect(Collectors.toList());

		List<String> monthlyLabels = fiveMonthsAgo.withDayOfMonth(1) // 시작 날짜를 해당 달의 첫 번째 날로 설정
				.datesUntil(today.plusMonths(1).withDayOfMonth(1), Period.ofMonths(1))
				.map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"))).collect(Collectors.toList());

		// 매출 데이터를 날짜별로 매핑
		Map<String, BigDecimal> dailySalesMap = salesData.stream().collect(Collectors.groupingBy(
				dto -> dto.getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
				Collectors.mapping(SalesDTO::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// 매출 데이터를 월별로 매핑
		Map<String, BigDecimal> monthlySalesMap = salesData.stream().collect(Collectors.groupingBy(
				dto -> dto.getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
				Collectors.mapping(SalesDTO::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// 날짜별 매출 데이터 생성(데이터가 없는 날짜엔 0으로 채움)
		List<BigDecimal> dailyData = dailyLabels.stream().map(date -> dailySalesMap.getOrDefault(date, BigDecimal.ZERO))
				.collect(Collectors.toList());

		List<BigDecimal> lastWeekData = lastWeekLabels.stream()
				.map(date -> dailySalesMap.getOrDefault(date, BigDecimal.ZERO)).collect(Collectors.toList());

		// 월별 매출 데이터 생성 (없는 날짜는 0으로 채움)
		List<BigDecimal> monthlyData = monthlyLabels.stream()
				.map(date -> monthlySalesMap.getOrDefault(date, BigDecimal.ZERO)).collect(Collectors.toList());

		model.addAttribute("userId", userId);
		model.addAttribute("facilities", responseFacilityDTO.getDtoList());
		model.addAttribute("reservations", responseReservationDTO.getDtoList());
		model.addAttribute("dailyLabels", dailyLabels);
		model.addAttribute("dailyData", dailyData);
		model.addAttribute("lastWeekData", lastWeekData);
		model.addAttribute("lastWeekLabels", lastWeekLabels);
		model.addAttribute("monthlyLabels", monthlyLabels);
		model.addAttribute("monthlyData", monthlyData);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Admin_Main";
	}

	@GetMapping("/Admin_edit")
	public String editAdmin(@AuthenticationPrincipal UserDetails userDetails, Model model) {

		String userId = userDetails.getUsername();
		model.addAttribute("userId", userId);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}
		return "admin/Admin_edit";
	}

	// Get 시설 등록
	@GetMapping("/Facility_add")
	public String addFacilityPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = userDetails.getUsername();
		model.addAttribute("userId", userId);
		model.addAttribute("facilityDTO", new FacilityDTO()); // 빈 DTO 객체 전달

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}
		return "admin/Facility_add"; // 입력 페이지로 이동
	}

	// 시설 수정하기 Get
	@GetMapping("/Facility_edit/{facilityCode}")
	public String EditFaciltyPage(@PathVariable("facilityCode") String facilityCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String userId = userDetails.getUsername();

		// 시설 정보를 가져오기 위해 서비스 호출
		FacilityDTO facilityDTO = facilityService.getFacilityByCode(facilityCode);

		// 모델에 로그인 정보를 추가하여 뷰로 전달
		model.addAttribute("userId", userId);
		// 모델에 시설 정보를 추가하여 뷰로 전달
		model.addAttribute("facility", facilityDTO);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Facility_edit";
	}

	// 시설 상세보기
	@GetMapping("/Facility_detail/{facilityCode}")
	public String DetailFaciltyPage(@PathVariable("facilityCode") String facilityCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String userId = userDetails.getUsername();

		// 시설 정보를 가져오기 위해 서비스 호출
		FacilityDTO facilityDTO = facilityService.getFacilityByCode(facilityCode);

		// 모델에 로그인 정보를 추가하여 뷰로 전달
		model.addAttribute("userId", userId);
		// 모델에 시설 정보를 추가하여 뷰로 전달
		model.addAttribute("facility", facilityDTO);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Facility_detail";
	}

	// 시설 리스트 보기
	@GetMapping("/Facility_list")
	public String ListFaciltyPage(PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		PageResponseDTO<FacilityDTO> responseDTO = facilityService.listByUser(memId, pageRequestDTO);

		// 삭제되지 않은 시설만 필터링(soft Delete)
		List<FacilityDTO> filteredList = responseDTO.getDtoList().stream().filter(facility -> !facility.isDeleteFlag())
				.collect(Collectors.toList());

		// 넘버링 계산(물리적/softDelete방식으로 삭제했을 때 넘버링)
		int startNumber = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize() + 1;
		for (int i = 0; i < filteredList.size(); i++) {
			filteredList.get(i).setNumber(startNumber + i);
		}

		model.addAttribute("memId", memId);

		// model.addAttribute("facilities", responseDTO.getDtoList());
		model.addAttribute("facilities", filteredList); // 필터링된 리스트를 전달
		model.addAttribute("totalPages", responseDTO.getTotal());
		model.addAttribute("pageNumber", pageRequestDTO.getPage()); // 현재 페이지 번호
		model.addAttribute("pageSize", pageRequestDTO.getSize()); // 한 페이지당 항목 수

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Facility_list";
	}

//	 @GetMapping("/Reservation_list")
//	 public String ListReservationPage(PageRequestDTO pageRequestDTO, Model model, 
//	     @AuthenticationPrincipal UserDetails userDetails) {
//	     

//	     String memId = userDetails.getUsername();
//	     
//	     // 로그인 한 유저의 시설 코드들 받아오기 
//	     List<FacilityDTO> facilityDTOs = facilityService.getFacilityCodeByUser(userDetails.getUsername());
//	     
//	     // 모든 시설의 예약 정보를 담을 리스트
//	     List<ReservationDTO> allReservations = new ArrayList<>();
//	     
//	     // 각 시설의 예약 정보 조회 
//	     for (FacilityDTO facility : facilityDTOs) {
//	         PageResponseDTO<ReservationDTO> responseDTO = 
//	             reservationService.getReservationByFacilityCode(facility.getFacilityCode(), pageRequestDTO);
//	         allReservations.addAll(responseDTO.getDtoList());
//	     }
//	     
//	     // 페이징 처리를 위한 계산
//	     int totalItems = allReservations.size();
//	     int pageSize = pageRequestDTO.getSize();
//	     int totalPages = (int) Math.ceil((double) totalItems / pageSize);
//	     
//	     model.addAttribute("memId", memId);
//	     model.addAttribute("facilities", facilityDTOs);
//	     model.addAttribute("reservations", allReservations);
//	     model.addAttribute("totalPages", totalPages);
//	     model.addAttribute("pageNumber", pageRequestDTO.getPage());
//	     model.addAttribute("pageSize", pageRequestDTO.getSize());
//	     
//	     return "admin/Reservation_list";
//	 }

	@GetMapping("/Facility_delete/{facilityCode}")
	public String deleteFacility(@PathVariable("facilityCode") String facilityCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails, Object redirectAttributes) {
		// 시설 정보 조회
		Optional<Facility> optionalFacility = facilityRepository.findByFacilityCode(facilityCode);

		if (optionalFacility.isPresent()) {
			Facility facility = optionalFacility.get();
			// deleteFlag를 0으로 설정
			facility.setDeleteFlag(true);
			facilityRepository.save(facility);
		}

		String userId = userDetails.getUsername();

		// 시설 삭제로 인한 예약 취소 및 환불 처리
		facilityService.cancelAllBookingByFacilityCode(facilityCode);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(userId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "redirect:/admin/Facility_list";
	}

	// 예약 리스트
	@GetMapping("/Reservation_list")
	public String ListReservationPage(PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		PageResponseDTO<ReservationDTO> responseDTO = reservationService.getAllReservationsForUser(memId,
				pageRequestDTO);

		model.addAttribute("memId", memId);
		model.addAttribute("reservations", responseDTO.getDtoList());
		model.addAttribute("totalPages", responseDTO.getTotal());
		model.addAttribute("pageNumber", pageRequestDTO.getPage());
		model.addAttribute("pageSize", pageRequestDTO.getSize());

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Reservation_list";
	}

	// 예약 상세보기
	@GetMapping("/Reservation_detail/{reservationCode}")
	public String DetailReservationPage(@PathVariable("reservationCode") String reservationCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		// 예약 정보를 가져오기 위해 서비스 호출
		ReservationDTO reservationDTO = reservationService.getReservationByCode(reservationCode);

		// 모델에 로그인 정보를 추가하여 뷰로 전달
		model.addAttribute("memId", memId);
		// 모델에 시설 정보를 추가하여 뷰로 전달
		model.addAttribute("reservation", reservationDTO);

		// Member 객체를 가져오는 로직 추가 [사용자정보]
		Optional<Member> memberOptional = memberRepository.findById(reservationDTO.getMemId());
		if (memberOptional.isPresent()) {
			model.addAttribute("member", memberOptional.get());
		}

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Reservation_detail";
	}

	// 승인 거절
	@GetMapping("/Reservation_refuse/{reservationCode}")
	@SuppressWarnings("unused")
	public String refuseReservation(TransferHistoryDTO transferHistoryDTO,
			@PathVariable("reservationCode") String reservationCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		// 예약 정보를 가져오기 위해 서비스 호출
		ReservationDTO reservationDTO = reservationService.getReservationByCode(reservationCode);

		// DTO를 엔티티로 변환하고 상태 변경
		Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
				.orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

		String memId = userDetails.getUsername();
		facilityService.cancelBookingbyManager(memId, transferHistoryDTO, reservationDTO);

		// 업데이트된 사용자 정보 가져오기
		Member updatedMember = memberRepository.findById(memId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		UserDetails updatedUser = customUserDetailsService.loadUserByUsername(updatedMember.getMemId());

		// 새 인증 정보 생성
		Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
				updatedUser.getAuthorities());

		// 보안 컨텍스트 갱신
		SecurityContextHolder.getContext().setAuthentication(newAuth);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		managerOptional.ifPresent(member -> model.addAttribute("manager", member));

		return "redirect:/admin/Reservation_list";
	}

	// 예약 승인
	@GetMapping("/Reservation_confirm/{reservationCode}")
	public String confirmReservation(TransferHistoryDTO transferHistoryDTO,
			@PathVariable("reservationCode") String reservationCode, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		// 예약 정보를 가져오기 위해 서비스 호출
		// ReservationDTO reservationDTO =
		// reservationService.getReservationByCode(reservationCode);

		// DTO를 엔티티로 변환하고 상태 변경
		Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
				.orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
		String payCode = reservation.getPayCode();

		String memId = userDetails.getUsername();

		// 만약 관리자가 예약거절했다가 승인할 경우
		if (reservation.isDeleteFlag() == true) {
			// 예약 정보를 가져오기 위해 서비스 호출
			ReservationDTO reservationDTO = reservationService.getReservationBypayCodeAndDeleteFlag(payCode);
			facilityService.cancelAndBookAgainbyManager(memId, transferHistoryDTO, reservationDTO);
		} else {
			// 예약 상태 변경
			reservation.setReservationProgress("예약완료");
			reservation.setMemo("관리자의 예약 승인");
			reservationRepository.save(reservation);
		}

		// 업데이트된 사용자 정보 가져오기
		Member updatedMember = memberRepository.findById(memId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		UserDetails updatedUser = customUserDetailsService.loadUserByUsername(updatedMember.getMemId());

		// 새 인증 정보 생성
		Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
				updatedUser.getAuthorities());

		// 보안 컨텍스트 갱신
		SecurityContextHolder.getContext().setAuthentication(newAuth);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}
		return "redirect:/admin/Reservation_list";
	}

	// 예약 승인 리스트
	@GetMapping("/Reservation_Confirmlist")
	public String ListReservationConfirmPage(PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		PageResponseDTO<ReservationDTO> responseDTO = reservationService.getConfirmReservationsForUser(memId,
				pageRequestDTO);

		model.addAttribute("memId", memId);
		model.addAttribute("reservations", responseDTO.getDtoList());
		model.addAttribute("totalPages", responseDTO.getTotal());
		model.addAttribute("pageNumber", pageRequestDTO.getPage());
		model.addAttribute("pageSize", pageRequestDTO.getSize());

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Reservation_confirmlist";
	}

	// 예약 취소 리스트
	@GetMapping("/Reservation_Refuselist")
	public String ListReservationRefusPage(PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		PageResponseDTO<ReservationDTO> responseDTO = reservationService.getRefuseReservationsForUser(memId,
				pageRequestDTO);

		model.addAttribute("memId", memId);
		model.addAttribute("reservations", responseDTO.getDtoList());
		model.addAttribute("totalPages", responseDTO.getTotal());
		model.addAttribute("pageNumber", pageRequestDTO.getPage());
		model.addAttribute("pageSize", pageRequestDTO.getSize());

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Reservation_refuselist";
	}

	// 예약 진행 리스트
	@GetMapping("/Reservation_InProgresslist")
	public String ListReservationInProgressPage(PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();

		PageResponseDTO<ReservationDTO> responseDTO = reservationService.getInprogressReservationsForUser(memId,
				pageRequestDTO);

		model.addAttribute("memId", memId);
		model.addAttribute("reservations", responseDTO.getDtoList());
		model.addAttribute("totalPages", responseDTO.getTotal());
		model.addAttribute("pageNumber", pageRequestDTO.getPage());
		model.addAttribute("pageSize", pageRequestDTO.getSize());

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/Reservation_inProgresslist";
	}

	// 캘린더 페이지 보여주기
	@GetMapping("/calendar")
	public String showCalendar(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		String memId = userDetails.getUsername();

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		return "admin/calendar";
	}

	// 예약 데이터를 JSON으로 반환하는 API
	@GetMapping("/calendar/events")
	@ResponseBody
	public List<Map<String, Object>> getConfirmedReservations(@AuthenticationPrincipal UserDetails userDetails) {

		String memId = userDetails.getUsername();
		List<ReservationDTO> confirmedReservations = reservationService.getConfirmedReservationsForUser(memId);

		List<Map<String, Object>> events = new ArrayList<>();

		for (ReservationDTO reservation : confirmedReservations) {
			Map<String, Object> event = new HashMap<>();
			event.put("title", reservation.getFacilityName());
			// start와 end 속성 사용
			event.put("start", reservation.getReservationDate() + "T" + reservation.getReservationStartTime());
			event.put("end", reservation.getReservationDate() + "T" + reservation.getReservationEndTime());
			event.put("id", reservation.getReservationCode());
			event.put("status", reservation.getReservationProgress());
			events.add(event);
		}

		return events;
	}

//    @GetMapping("/revenue")
//    public String showRevenu(@AuthenticationPrincipal UserDetails userDetails
//    						, Model model
//    						,PageRequestDTO pageRequestDTO) {
//      
//    	String memId = userDetails.getUsername();
//      
//    	PageResponseDTO<ReservationDTO> responseDTO = reservationService.getAllReservationsForUser(memId,pageRequestDTO);
//
//    	List<SalesDTO> salesData = revenueService.getSalesData(memId);
//
//    	//차트에 필요한 데이터 분리(일자별 매출합계)
//    	Map<String,BigDecimal> dailySales 
//    	= salesData.stream()
//    	           .collect(Collectors.groupingBy(dto -> dto.getTransferDate()
//    	        		   			  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
//    	        		   			  		  Collectors.mapping(SalesDTO::getAmount,Collectors.reducing(BigDecimal.ZERO,BigDecimal::add))));
//    	
//    	List<String> dailyLabels = new ArrayList<>(dailySales.keySet());
//    	
//		model.addAttribute("memId", memId);
//		model.addAttribute("reservations", responseDTO.getDtoList());
//		model.addAttribute("dailyLabels",dailyLabels);
//		model.addAttribute("dailyData",new ArrayList<>(dailySales.values()));
//		
//
//        
//        // Member 객체를 가져오는 로직 추가 [관리자정보]
//        Optional<Member> managerOptional = memberRepository.findById(memId);
//        if (managerOptional.isPresent()) {
//            model.addAttribute("manager", managerOptional.get());
//        }
//        
//        return "admin/Admin_Revenue";
//    }

	@GetMapping("/revenue")
	public String showRevenu(@AuthenticationPrincipal UserDetails userDetails, Model model,
			PageRequestDTO pageRequestDTO) {

		String memId = userDetails.getUsername();
		List<ReservationDTO> responseDTO = reservationService.getAllReservationsForUser(memId);

		// 매출 데이터 가져오기
		List<SalesDTO> salesData = revenueService.getSalesData(memId);
		System.out.println("Sales Data: " + salesData);

		// 날짜 범위 생성 (지난 7일)
		LocalDate today = LocalDate.now();
		LocalDate sevenDaysAgo = today.minusDays(6);

		// 날짜 범위 생성(지난 5개월)
		LocalDate fiveMonthsAgo = today.minusMonths(4);

		List<String> dailyLabels = sevenDaysAgo.datesUntil(today.plusDays(1)).map(LocalDate::toString)
				.collect(Collectors.toList());

		List<String> monthlyLabels = fiveMonthsAgo.withDayOfMonth(1) // 시작 날짜를 해당 달의 첫 번째 날로 설정
				.datesUntil(today.plusMonths(1).withDayOfMonth(1), Period.ofMonths(1))
				.map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"))).collect(Collectors.toList());

		// 매출 데이터를 날짜별로 매핑
		Map<String, BigDecimal> dailySalesMap = salesData.stream().collect(Collectors.groupingBy(
				dto -> dto.getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
				Collectors.mapping(SalesDTO::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// 매출 데이터를 월별로 매핑
		Map<String, BigDecimal> monthlySalesMap = salesData.stream().collect(Collectors.groupingBy(
				dto -> dto.getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
				Collectors.mapping(SalesDTO::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// 날짜별 매출 데이터 생성 (없는 날짜는 0으로 채움)
		List<BigDecimal> dailyData = dailyLabels.stream().map(date -> dailySalesMap.getOrDefault(date, BigDecimal.ZERO))
				.collect(Collectors.toList());

		// 월별 매출 데이터 생성 (없는 날짜는 0으로 채움)
		List<BigDecimal> monthlyData = monthlyLabels.stream()
				.map(date -> monthlySalesMap.getOrDefault(date, BigDecimal.ZERO)).collect(Collectors.toList());

		System.out.println("Daily Sales Map: " + dailySalesMap);
		System.out.println("Daily Labels: " + dailyLabels);
		System.out.println("Daily Data: " + dailyData);

		System.out.println("monthly Sales Map: " + monthlySalesMap);
		System.out.println("monthly Labels: " + monthlyLabels);
		System.out.println("monthly Data: " + monthlyData);

		// Member 객체를 가져오는 로직 추가 [관리자정보]
		Optional<Member> managerOptional = memberRepository.findById(memId);
		if (managerOptional.isPresent()) {
			model.addAttribute("manager", managerOptional.get());
		}

		model.addAttribute("memId", memId);
		model.addAttribute("reservations", responseDTO);
		model.addAttribute("dailyLabels", dailyLabels);
		model.addAttribute("dailyData", dailyData);
		model.addAttribute("monthlyLabels", monthlyLabels);
		model.addAttribute("monthlyData", monthlyData);

		return "admin/Admin_Revenue";
	}

}