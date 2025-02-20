package com.lec.packages.controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lec.packages.domain.Member;
import com.lec.packages.domain.Reservation;
import com.lec.packages.dto.ChargeHistoryDTO;
import com.lec.packages.dto.ClubDTO;
import com.lec.packages.dto.MemberJoinDTO;
import com.lec.packages.dto.MemberSecurityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.repository.ReservationRepository;
import com.lec.packages.security.CustomUserDetailsService;
import com.lec.packages.service.ClubService;
import com.lec.packages.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

	@Value("${com.lec.upload.path}")
	private String uploadPath;

	private final MemberService memberService;
	private final CustomUserDetailsService customUserDetailsService;
	private final MemberRepository memberRepository;
	private final ReservationRepository reservationRepository;
	private final PasswordEncoder passwordEncoder;

	private final ClubService clubService;

	@GetMapping({ "/login/{logout}" })
	public void loginGet(@RequestParam(name = "logout", defaultValue = "") @PathVariable Optional<String> logout,
			Model model) {
		log.info("login get ................... ");
		log.info("logout ................... " + logout);

		if (logout != null) {
			log.info("user logout ................... ");
		}

	}

	@GetMapping("/login")
	public void loginErrorGet(@RequestParam(name = "error", required = false) String error, Model model) {
		if (error != null) {
			model.addAttribute("errorMessage", "아이디 또는 비밀번호가 잘못되었습니다.");
		}

	}

	@GetMapping("/join")
	public void joinGet() {
		log.info("회원가입 GET방식.....");
	}

	@PostMapping("/join")
	public String joinPost(MemberJoinDTO memberJoinDTO, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		log.info("회원가입 POST방식.....");
		log.info(memberJoinDTO);

		// 세션에서 업로드된 파일 이름 가져오기
		HttpSession session = request.getSession();
		String storedFileName = (String) session.getAttribute("storedFileName");

		// 회원가입 처리
		memberService.join(memberJoinDTO, storedFileName);
		redirectAttributes.addFlashAttribute("result", "회원가입 성공");

		// 회원가입 성공 후 세션에서 파일 이름 제거
		session.removeAttribute("storedFileName");

		return "redirect:/member/login";
	}

	@GetMapping("/checkId")
	public ResponseEntity<Map<String, String>> checkId(@RequestParam("MEM_ID") String memId) {
		Map<String, String> response = new HashMap<>();

		try {
			boolean isDuplicate = memberService.isDuplicateId(memId);

			if (isDuplicate) {
				response.put("status", "FAIL");
				response.put("message", "이미 존재하는 아이디입니다.");
			} else {
				response.put("status", "SUCCESS");
				response.put("message", "사용 가능한 아이디입니다.");
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put("status", "ERROR");
			response.put("message", "서버 처리 중 문제가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/mypage")
	public String mypageGet(HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO dto = (MemberSecurityDTO) authentication.getPrincipal();

			// Member 객체를 가져오는 로직 추가
			Optional<Member> memberOptional = memberRepository.findById(dto.getMemId());
			if (memberOptional.isPresent()) {
				model.addAttribute("member", memberOptional.get());
			}
		}
		return "member/mypage";
	}

	@GetMapping("/mypage_modify")
	public String mypageModifyGet(HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI); // 템플릿에서 사용된다면 유지

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO dto = (MemberSecurityDTO) authentication.getPrincipal();

			// Member 객체를 가져오는 로직 추가
			Optional<Member> memberOptional = memberRepository.findById(dto.getMemId());
			if (memberOptional.isPresent()) {
				model.addAttribute("member", memberOptional.get());
			}
		}
		return "member/mypage_modify";
	}

	@PostMapping("/modify")
	public String mypageModifyPost(MemberJoinDTO memberJoinDTO, HttpServletRequest request,
			RedirectAttributes redirectAttributes, Principal principal) {
		HttpSession session = request.getSession();
		String storedFileName = (String) session.getAttribute("storedFileName");
		if (storedFileName == null) {
			storedFileName = memberJoinDTO.getMemPicture();
		}

		try {

//			// 기존 사용자 정보 가져오기
//		    Optional<Member> existingMember = memberRepository.findById(principal.getName());
//		    
//			// 비밀번호 필드가 비어 있으면 기존 비밀번호 유지
//		    if (memberJoinDTO.getMemPw() == null || memberJoinDTO.getMemPw().isEmpty()) {
//		    	memberJoinDTO.setMemPw(existingMember.get().getMemPw()); // 기존 비밀번호 유지
//		    } else {
//		        // 새 비밀번호가 입력되었으면 암호화 후 저장
//		    	memberJoinDTO.setMemPw(passwordEncoder.encode(memberJoinDTO.getMemPw()));
//		    }

			// 회원 정보 수정
			memberService.modify(memberJoinDTO, storedFileName);

			// 수정된 사용자 정보 가져오기
			UserDetails updatedUser = customUserDetailsService.loadUserByUsername(memberJoinDTO.getMemId());

			// 새 인증 정보 생성
			Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
					updatedUser.getAuthorities());

			// 보안 컨텍스트 갱신
			SecurityContextHolder.getContext().setAuthentication(newAuth);

			redirectAttributes.addFlashAttribute("message", "나의 정보 수정 성공했습니다.");
		} catch (Exception e) {
			log.error("회원 정보 수정 실패", e);
			redirectAttributes.addFlashAttribute("message", "회원 정보 수정 중 오류가 발생했습니다.");
			return "redirect:/member/mypage_modify";
		}

		session.removeAttribute("storedFileName");
		return "redirect:/member/mypage";
	}

	// 회원 삭제
	@PostMapping("/remove")
	public String removeAccount(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		try {
			// 현재 인증된 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName(); // 현재 사용자 ID

			// 회원이 가입한 클럽 목록 조회
			List<String> clubCodes = clubService.findJoinClubCodeByMemId(username);

			// 회원이 가입한 클럽 탈퇴, 참여한 일정 삭제
			for (String clubCode : clubCodes) {
				clubService.joindelete(username, clubCode);
				clubService.removeClubResMember(clubCode, username);
			}

			// 회원 삭제 처리 (DELETE_FLAG를 1로 설정)
			memberService.remove(username);

			// 세션 무효화
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}

			// 보안 컨텍스트 초기화
			SecurityContextHolder.clearContext();

			redirectAttributes.addFlashAttribute("message", "계정이 성공적으로 삭제되었습니다.");
			return "redirect:/member/login"; // 로그인 페이지로 이동
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", "계정 삭제 중 오류가 발생했습니다.");
			return "redirect:/member/mypage";
		}
	}

	// 마이페이지 나의 시설 예약 조회
	@GetMapping("/reservation")
	public String reservationGet(HttpServletRequest request, Model model) {
		// 현재 요청 URI를 모델에 추가
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);

		// 인증 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberSecurityDTO)) {
			model.addAttribute("error", "로그인 정보가 필요합니다.");
			return "redirect:/login"; // 로그인 페이지로 리다이렉트
		}

		// 로그인한 사용자 정보 가져오기
		MemberSecurityDTO memberSecurity = (MemberSecurityDTO) authentication.getPrincipal();
		String memId = memberSecurity.getMemId(); // 현재 로그인한 사용자의 ID

		// 사용자 정보 가져오기
		Member member = memberRepository.findById(memId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		model.addAttribute("member", member); // 사용자 정보를 모델에 추가

		// 예약 정보 가져오기
		List<Reservation> reservations = reservationRepository.findByMemId(memId);
		if (!reservations.isEmpty()) {
		    model.addAttribute("reservations", reservations); // 예약 목록 추가
		    
		    // ✅ 모든 clubCode를 리스트로 추출 (중복 제거)
		    List<String> clubCodes = reservations.stream()
		                                         .map(Reservation::getClubCode)
		                                         .filter(Objects::nonNull)
		                                         .distinct()
		                                         .collect(Collectors.toList());

		    // ✅ clubCode 리스트를 한 번의 쿼리로 조회하여 맵으로 변환
		    Map<String, String> clubNameMap = clubService.getClubNamesByCodes(clubCodes);
		    
		    model.addAttribute("clubNameMap", clubNameMap); 
		} else {
		    model.addAttribute("noReservations", "예약이 없습니다."); // 예약이 없는 경우 메시지 추가
		}

		// 보안 컨텍스트 갱신 (선택 사항, 필요하면 유지)
		UserDetails updatedUser = customUserDetailsService.loadUserByUsername(memId);
		Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
				updatedUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);

		return "member/reservation"; // 반환할 뷰 이름
	}

	// 금액 충전
	@GetMapping("/charge/point")
	public String myCashPro(@RequestParam("amount") BigDecimal amount, @RequestParam("plusPoint") BigDecimal plusPoint,
			HttpServletRequest request, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO member = (MemberSecurityDTO) authentication.getPrincipal();
			String memId = member.getMemId(); // Current logged-in user's ID

			memberService.chargePoint(memId, amount, plusPoint);

			// 업데이트된 사용자 정보 가져오기
			Member updatedMember = memberRepository.findById(memId)
					.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
			UserDetails updatedUser = customUserDetailsService.loadUserByUsername(updatedMember.getMemId());

			// 새 인증 정보 생성
			Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, updatedUser.getPassword(),
					updatedUser.getAuthorities());

			// 보안 컨텍스트 갱신
			SecurityContextHolder.getContext().setAuthentication(newAuth);

		}
		;
		// 이전 페이지 URL 가져오기
		String referer = request.getHeader("Referer");
		if (referer != null) {
			return "redirect:" + referer; // 이전 페이지로 리다이렉트
		}
		return "redirect:/member/mypage";
	}

	// 마이페이지 송금 내역 조회
	@GetMapping("/pay_list/transfer")
	@ResponseBody
	public List<TransferHistoryDTO> getTransferHistories() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO member = (MemberSecurityDTO) authentication.getPrincipal();
			String memId = member.getMemId();
			// Return transfer history
			return memberService.getTransferHistories(memId);
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 필요합니다.");
		}
	}

	// 관리자계정일 경우 송금내역 조회
	@GetMapping("/pay_list/transfer_manager")
	@ResponseBody
	public List<TransferHistoryDTO> getTransferHistoriesWhenManager() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO member = (MemberSecurityDTO) authentication.getPrincipal();
			String memId = member.getMemId();
			// Return transfer history
			return memberService.getTransferHistorieWhenManager(memId);
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 필요합니다.");
		}
	}

	// 마이페이지 충전 내역 조회
	@GetMapping("/pay_list/charge")
	@ResponseBody
	public List<ChargeHistoryDTO> getChargeHistories() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO member = (MemberSecurityDTO) authentication.getPrincipal();
			String memId = member.getMemId();
			// Return charge history
			return memberService.getChargeHistories(memId);
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 필요합니다.");
		}
	}

	@GetMapping("/club_myclub")
	public String clubManage(PageRequestDTO pageRequestDTO,
			@RequestParam(value = "clubCode", required = false) String clubCode, Authentication authentication,
			HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		String memId = authentication.getName();

		List<ClubDTO> ownerClubList = clubService.ownerClubListWithMemId(memId);
		log.info("ownerClubList: {}", ownerClubList);

		PageResponseDTO<Member> responseDTO = null;
		if (clubCode != null) {
			responseDTO = clubService.findMemberAll(clubCode, pageRequestDTO);
		}
		
		// 사용자 정보 가져오기
		Member member = memberRepository.findById(memId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		model.addAttribute("member", member); // 사용자 정보를 모델에 추가

		model.addAttribute("responseDTO", responseDTO);
		model.addAttribute("clubCode", clubCode);
		model.addAttribute("currentURI", requestURI);
		model.addAttribute("ownerClubList", ownerClubList);
		model.addAttribute("memId", memId);

		return "member/club_myclub";
	}

	// 신고 3회이상 회원 탈퇴
	@PostMapping("/club_myclubjoindel")
	public String clubJoinString(@RequestParam(value = "clubCode") String clubCode,
			@RequestParam(value = "memId") String memId, HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();

		clubService.joindelete(memId, clubCode);
		clubService.removeClubResMember(clubCode, memId);
		model.addAttribute("currentURI", requestURI);

		return "redirect:/member/club_myclub?clubCode=" + clubCode;
	}

	// 가입한 클럽보기
	@GetMapping("/club_joinclub")
	public String clubJoinList(PageRequestDTO pageRequestDTO,
			@RequestParam(value = "clubCode", required = false) String clubCode, 
			Authentication authentication,
			HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		String memId = authentication.getName();

		// 클럽장인 클럽 제외한 클럽만 가져오기
		List<ClubDTO> myClubList = clubService.clubListWithMemID(memId);	
		List<ClubDTO> ownerClubList = clubService.ownerClubListWithMemId(memId);
		List<ClubDTO> filterClubList = myClubList.stream()
				.filter(club -> ownerClubList.stream().noneMatch(ownerClub -> ownerClub.getClubCode().equals(club.getClubCode())))
				.collect(Collectors.toList());		
		
		// 사용자 정보 가져오기
		Member member = memberRepository.findById(memId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		model.addAttribute("member", member); // 사용자 정보를 모델에 추가
		
		model.addAttribute("clubCode", clubCode);
		model.addAttribute("currentURI", requestURI);
		model.addAttribute("myClubList", filterClubList);
		model.addAttribute("memId", memId);

		return "member/club_joinclub";
	}


	// 비밀번호 찾기
	@GetMapping("/find_pw")
	public String findPwGet(HttpServletRequest request, Model model) {

		return "member/find_pw";
	}

	@PostMapping("/find_pw")
	public String findPassword(@RequestParam("memId") String memId, @RequestParam("memName") String memName,
			RedirectAttributes redirectAttributes) {
		boolean success = memberService.processFindPassword(memId, memName);

		if (success) {
			redirectAttributes.addFlashAttribute("message", "임시 비밀번호가 이메일로 전송되었습니다.");
			return "redirect:/member/login";
		} else {
			redirectAttributes.addFlashAttribute("message", "해당하는 회원 정보를 찾을 수 없습니다.");
			return "redirect:/member/find_pw";
		}

	}

	@GetMapping("/email-template")
	public String getEmailTemplate(HttpServletRequest request, Model model) {

		return "member/email-template";
	}

	// 운동캘린더 조회
	@GetMapping("/member_planner")
	public String plannerGet(HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
			MemberSecurityDTO dto = (MemberSecurityDTO) authentication.getPrincipal();

			// Member 객체를 가져오는 로직 추가
			Optional<Member> memberOptional = memberRepository.findById(dto.getMemId());
			if (memberOptional.isPresent()) {
				model.addAttribute("member", memberOptional.get());
			}
		}
		return "member/member_planner";
	}

}
