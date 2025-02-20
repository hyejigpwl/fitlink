package com.lec.packages.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lec.packages.domain.Member_Planner;
import com.lec.packages.domain.Reservation;
import com.lec.packages.domain.Reservation_Member_List;
import com.lec.packages.dto.ReservationDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.ClubRepository;
import com.lec.packages.repository.ClubReservationMemberRepository;
import com.lec.packages.repository.MemberPlannerRepository;
import com.lec.packages.repository.ReservationRepository;
import com.lec.packages.security.CustomUserDetailsService;
import com.lec.packages.service.ClubService;
import com.lec.packages.service.MemberPlannerService;
import com.lec.packages.service.ReservationService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/planner")
@RequiredArgsConstructor
public class MemberPlannerController {

	private final MemberPlannerService memberPlannerService;
	private final ClubService clubService;
	@Autowired
	private final CustomUserDetailsService customUserDetailsService;
	private final ReservationService reservationService;
	private final ClubRepository clubRepository;
	private final ReservationRepository reservationRepository;
	private final MemberPlannerRepository memberPlannerRepository;
	private final ClubReservationMemberRepository clubReservationMemberRepository;

	@GetMapping("/list")
	public ResponseEntity<List<Map<String, Object>>> getPlanners(@RequestParam("memId") String memId) {
	    List<Map<String, Object>> formattedPlanners = new ArrayList<>();

	    try {
	        // 1️⃣ 일반 일정(Member_Planner) 가져오기
	        List<Member_Planner> planners = memberPlannerService.findNonClubPlannersByMemId(memId);

	        for (Member_Planner planner : planners) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", planner.getPlanNo());
	            map.put("title", planner.getPlanName());
	            map.put("start", planner.getPlanDate().toString());
	            map.put("planText", planner.getPlanText());
	            map.put("planIschk", planner.isPlanIschk());
	            map.put("planIsclub", planner.isPlanIsclub());
	            formattedPlanners.add(map);
	        }

	        // 2️⃣ 클럽 일정(Reservation_Member_List) 가져오기
	        List<Reservation_Member_List> clubReservations = memberPlannerService.getClubReservations(memId);

	        for (Reservation_Member_List res : clubReservations) {
	            // ✅ 이미 등록된 일정인지 확인
	            Optional<Member_Planner> existingPlanner = memberPlannerRepository.findFirstByReservationCodeAndMemId(res.getReservationCode(),res.getMemId());

	            String clubName = clubRepository.findClubNameByClubCode(res.getClubCode());
	            
	            List<Object[]> results = reservationRepository.findFacilityAndTimesByCode(res.getReservationCode());
	            Object[] result = results.stream().findFirst().orElse(new Object[]{"시설 정보 없음", null, null}); // ✅ 기본값 설정


	            String facilityName = (result[0] instanceof String) ? (String) result[0] : "시설 정보 없음";
	            String startTime = (result[1] instanceof java.time.LocalTime) ? result[1].toString() : "시작 시간 없음";
	            String endTime = (result[2] instanceof java.time.LocalTime) ? result[2].toString() : "종료 시간 없음";


	            if (existingPlanner.isEmpty()) { // ✅ 중복 등록 방지
	                // ✅ DB에 클럽 일정 추가
	                Member_Planner newPlanner = Member_Planner.builder()
	                        .memId(res.getMemId())
	                        .planDate(res.getReservationDate())
	                        // planNo(existingPlanner.get().getPlanNo())
	                        .planName("[클럽] " + clubName)
	                        .planText("장소 : " + facilityName + "\n시간 :" + startTime + "~" + endTime)
	                        .planIschk(false)
	                        .planIsclub(true)
	                        .reservationCode(res.getReservationCode())
	                        .deleteFlag(false)
	                        .build();

	                memberPlannerService.savePlanner(newPlanner); // ✅ 일정 저장

	               
	            }
	            
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", res.getMemId()+res.getReservationCode());
                map.put("title", "[클럽] " + clubName);
                map.put("start", res.getReservationDate().toString());
                map.put("planText", "장소 : " + facilityName + "\n시간 :" + startTime + "~" + endTime);
                map.put("planIschk", false);
                map.put("planIsclub", true);
                map.put("color", "#ff9800");
                map.put("reservationCode", res.getReservationCode());  // 예약 코드 추가
                map.put("clubCode", res.getClubCode());  // 클럽 코드 추가
                map.put("memId", res.getMemId());  // 예약한 사용자 ID 추가

                formattedPlanners.add(map);
	        }
	        
	     // 3. 개인 시설 예약 일정(Reservation_Member_List) 가져오기
	        List<Reservation> memberReservations = memberPlannerService.getMemberReservations(memId);

	        for (Reservation res : memberReservations) {
	            // ✅ 이미 등록된 일정인지 확인
	            Optional<Member_Planner> existingPlanner = memberPlannerRepository.findFirstByReservationCodeAndMemId(res.getReservationCode(),res.getMemId());

	            
	            List<Object[]> results = reservationRepository.findFacilityAndTimesByCode(res.getReservationCode());
	            Object[] result = results.stream().findFirst().orElse(new Object[]{"시설 정보 없음", null, null}); // ✅ 기본값 설정


	            String facilityName = (result[0] instanceof String) ? (String) result[0] : "시설 정보 없음";
	            String startTime = (result[1] instanceof java.time.LocalTime) ? result[1].toString() : "시작 시간 없음";
	            String endTime = (result[2] instanceof java.time.LocalTime) ? result[2].toString() : "종료 시간 없음";


	            if (existingPlanner.isEmpty()) { // ✅ 중복 등록 방지
	                // ✅ DB에 시설 예약 일정 추가
	                Member_Planner newPlanner = Member_Planner.builder()
	                        .memId(res.getMemId())
	                        .planDate(res.getReservationDate())
	                        // planNo(existingPlanner.get().getPlanNo())
	                        .planName("[시설예약] " + facilityName)
	                        .planText("장소 : " + facilityName + "\n시간 :" + startTime + "~" + endTime)
	                        .planIschk(false)
	                        .planIsclub(false)
	                        .reservationCode(res.getReservationCode())
	                        .deleteFlag(false)
	                        .build();

	                memberPlannerService.savePlanner(newPlanner); // ✅ 일정 저장

	               
	            }
	            
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", res.getMemId()+res.getReservationCode());
                map.put("title", "[시설예약] " + facilityName);
                map.put("start", res.getReservationDate().toString());
                map.put("planText", "장소 : " + facilityName + "\n시간 :" + startTime + "~" + endTime);
                map.put("planIschk", false);
                map.put("planIsclub", false);
                map.put("color", "#02b875");
                map.put("reservationCode", res.getReservationCode());  // 예약 코드 추가
                // map.put("clubCode", res.getClubCode());  // 클럽 코드 추가
                map.put("memId", res.getMemId());  // 예약한 사용자 ID 추가

                formattedPlanners.add(map);
	        }

	        return ResponseEntity.ok(formattedPlanners);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
	    }
	}


	// 일정 추가
	@PostMapping("/add")
	public Member_Planner addPlanner(@RequestBody Member_Planner planner) {
		return memberPlannerService.savePlanner(planner);
	}

	// 특정 날짜 일정 조회
	@GetMapping("/date")
	public List<Member_Planner> getPlannerByDate(@RequestParam("memId") String memId,
			@RequestParam("planDate") Date planDate) {
		return memberPlannerService.getPlannerByDate(memId, planDate);
	}

	@DeleteMapping("/delete")
	@Transactional
    public ResponseEntity<?> deletePlanner(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer planNo = (Integer) requestBody.get("planNo");
            String reservationCode = (String) requestBody.get("reservationCode");
            String clubCode = (String) requestBody.get("clubCode");
            String memId = (String) requestBody.get("memId");

            System.out.println("🛠️ [DELETE 요청] planNo: " + planNo + ", reservationCode: " + reservationCode + ", clubCode: " + clubCode + ", memId: " + memId);


            boolean deleted = memberPlannerService.deletePlanner(planNo, reservationCode, clubCode, memId);

            if (deleted) {
           
    			
    			if (memId == null) {
    				return ResponseEntity.ok().body("✅ 일정이 삭제되었습니다.");
    			}else {
    				// 수정된 사용자 정보 가져오기

    				UserDetails updatedUser = customUserDetailsService.loadUserByUsername(memId);

    				// 새 인증 정보 생성
    				Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
    						updatedUser.getAuthorities());

    				// 보안 컨텍스트 갱신
    				SecurityContextHolder.getContext().setAuthentication(newAuth);
    	    			return ResponseEntity.ok().body("✅ 일정이 삭제되었습니다.");
    		        }
    			
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("🚨 일정 삭제 실패");
            }
        } catch (Exception e) {
            System.err.println("❌ [에러 발생] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("🚨 삭제 중 오류 발생: " + e.getMessage());
        }
    }







	// ✅ 일정 완료 상태 업데이트 API (토글 기능)
	@PutMapping("/updateIschk")
	public ResponseEntity<String> updatePlanIschk(@RequestBody Member_Planner planner) {
		Member_Planner existingPlanner = memberPlannerService.getPlannerById(planner.getPlanNo());

		if (existingPlanner != null) {
			existingPlanner.setPlanIschk(planner.isPlanIschk()); // ✅ true/false 값 반영
			memberPlannerService.savePlanner(existingPlanner);
			System.out.println("📌 일정 완료 상태 변경됨: " + planner.getPlanNo() + " → " + planner.isPlanIschk());
			return ResponseEntity.ok("✅ 일정 상태 변경 성공!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("🚨 일정이 존재하지 않습니다.");
		}
	}

	// 특정 일정 조회 API (상세보기)
	@GetMapping("/detail")
	public Member_Planner getPlannerDetail(@RequestParam Integer planNo) {
		return memberPlannerService.getPlannerById(planNo);
	}

	// 클럽 일정 가져오기
//    @PostMapping("/convertReservations")
//    public ResponseEntity<String> convertReservations(@RequestParam String memId) {
//        memberPlannerService.saveReservationsAsPlans(memId);
//        return ResponseEntity.ok("예약 데이터가 일정으로 변환되었습니다.");
//    }
}
