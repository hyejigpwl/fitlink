package com.lec.packages.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.Member_Planner;
import com.lec.packages.domain.Reservation;
import com.lec.packages.domain.Reservation_Member_List;
import com.lec.packages.dto.ReservationDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.ChargeHistoryRepository;
import com.lec.packages.repository.ClubReservationMemberRepository;
import com.lec.packages.repository.ExerciseRepository;
import com.lec.packages.repository.MemberPlannerRepository;
import com.lec.packages.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberPlannerService {

	private final MemberPlannerRepository memberPlannerRepository;
	private final ClubReservationMemberRepository clubReservationMemberRepository;
	private final FacilityService facilityService;
	private final ClubService clubService;

	// 일정 저장
	public Member_Planner savePlanner(Member_Planner planner) {
		return memberPlannerRepository.save(planner);
	}

	// 특정 회원의 특정 날짜 일정 조회
	public List<Member_Planner> getPlannerByDate(String memId, Date planDate) {
		return memberPlannerRepository.findByMemIdAndPlanDate(memId, planDate);
	}

	// 특정 회원의 모든 일정 조회
	public List<Member_Planner> findNonClubPlannersByMemId(String memId) {
		return memberPlannerRepository.findByMemId(memId);
	}

	public Member_Planner getPlannerById(Integer planNo) {
		return memberPlannerRepository.findByPlanNoAndDeleteFlagFalse(planNo)
				.orElseThrow(() -> new IllegalArgumentException("해당 일정이 존재하지 않습니다. planNo: " + planNo));
	}

	public List<Reservation_Member_List> getClubReservations(String memId) {
		return clubReservationMemberRepository.findClubReservationsWithDetails(memId);
	}

	public List<Reservation> getMemberReservations(String memId) {
		return clubReservationMemberRepository.findMemberReservationsWithDetails(memId);
	}

	@Transactional
	public boolean deletePlanner(Integer planNo, String reservationCode, String clubCode, String memId) {
		try {
			

			if (planNo == null) {
				// 1. 일정 조회
				Member_Planner memberPlanner = memberPlannerRepository.findByReservationCodeAndMemIAndDeleteFlagFalse(reservationCode,memId)
						.orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. PlanNo: " + planNo));

				System.out.println("🛠️ [삭제 진행] 예약 코드: " + reservationCode + ", 클럽 코드: " + clubCode + ", 사용자 ID: " + memId);

				// 2. 클럽 예약 삭제 처리
				if (clubCode != null && !clubCode.trim().isEmpty()) {
					System.out.println("🛠️ [클럽 예약 삭제] 예약 코드: " + reservationCode + ", 클럽 코드: " + clubCode);
					boolean clubMemberRemoved = clubService.removeClubResMember(reservationCode, clubCode, memId)
							.equals("success");

					if (clubMemberRemoved) {
						System.out.println("✅ [클럽 예약 삭제 완료]");
						return true;
					} else {
						throw new RuntimeException("🚨 클럽 예약 삭제 실패");
					}
				}

				// 3. 일반 시설 예약 또는 일반 예약 삭제 처리
				if (reservationCode != null && !reservationCode.trim().isEmpty()) {
					System.out.println("🛠️ [시설 예약 또는 일반 예약 삭제] 예약 코드: " + reservationCode);

					TransferHistoryDTO transferHistoryDTO = new TransferHistoryDTO();
					ReservationDTO reservationDTO = new ReservationDTO();
				    reservationDTO.setReservationCode(reservationCode); // ✅ reservationCode 설정
					System.out.println("🛠️ [시설 예약 또는 일반 예약 삭제] 예약 코드: " + reservationCode);

					// ✅ `cancelBooking()` 호출 후 예외가 발생하지 않으면 성공 처리
					facilityService.cancelBooking(memId, transferHistoryDTO, reservationDTO);

					System.out.println("✅ [시설 예약 또는 일반 예약 삭제 완료]");
					return true;

				}
			}
			
			// ✅ planNo가 있을 경우 일반 삭제 처리
	        Member_Planner memberPlanner = memberPlannerRepository.findById(planNo)
	                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다. PlanNo: " + planNo));

			// 4. 개인 일정(예약 없이 직접 추가한 일정) 삭제 처리
			System.out.println("🛠️ [개인 일정 삭제] PlanNo: " + planNo);
			memberPlanner.setDeleteFlag(true);
			memberPlannerRepository.save(memberPlanner);
			memberPlannerRepository.flush(); // ✅ 즉시 DB 반영

			System.out.println("✅ [개인 일정 삭제 완료]");
			return true;

		} catch (Exception e) {
			System.err.println("🚨 [일정 삭제 중 오류 발생] " + e.getMessage());
			return false;
		}
	}

}
