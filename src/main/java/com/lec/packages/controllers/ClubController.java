package com.lec.packages.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lec.packages.domain.Member;
import com.lec.packages.dto.ClubBoardAllListDTO;
import com.lec.packages.dto.ClubBoardDTO;
import com.lec.packages.dto.ClubDTO;
import com.lec.packages.dto.MemberSecurityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.service.ClubService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubController {
	
    @Value("${com.lec.upload.path}")
    private String uploadPath;
	
	@Autowired
	private final ClubService clubService;
	private final MemberRepository memberRepository;
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/club_create")
	public String clubCreateGet(HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);
		return "club/club_create"; 
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/club_detail")
	public String clubDetail(@RequestParam("clubCode") String clubCode
			, PageRequestDTO pageRequestDTO
			, HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);
				
		ClubDTO clubDTO = clubService.detail(clubCode);		
		if(clubDTO.getClubTheme() != null && !clubDTO.getClubTheme().isEmpty()) {
			clubDTO.setClubTheme(clubDTO.getClubTheme());
		}
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		MemberSecurityDTO principal = (MemberSecurityDTO) authentication.getPrincipal();
		model.addAttribute("principal", principal);
		
		String memId = principal.getMemId();
		boolean isMember = clubService.isJoinMember(memId, clubCode);
		model.addAttribute("isMember", isMember);
		
		// 클럽상세보기에서 회원3명만 보여지기 제한
		List<Member> clubmembers = clubService.findMemberDetails(clubCode)
											  .stream()
											  .limit(3)
											  .collect(Collectors.toList());
        model.addAttribute("clubmembers", clubmembers);
        
        Map<String, Integer> memberCount = clubService.membercount();
		model.addAttribute("memberCount", memberCount);

        model.addAttribute("clubdto", clubDTO);
        
        return "club/club_detail";
	}
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/club_modify")
	public String clubModify(@RequestParam("clubCode") String clubCode
			, PageRequestDTO pageRequestDTO
			, HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);
		
		ClubDTO clubDTO = clubService.detail(clubCode);		
		if(clubDTO.getClubTheme() != null && !clubDTO.getClubTheme().isEmpty()) {
			clubDTO.setClubTheme(clubDTO.getClubTheme());
		}
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		MemberSecurityDTO principal = (MemberSecurityDTO) authentication.getPrincipal();
		model.addAttribute("principal", principal);
		
		String memId = principal.getMemId();
		boolean isMember = clubService.isJoinMember(memId, clubCode);
		model.addAttribute("isMember", isMember);
		
		// 클럽상세보기에서 회원3명만 보여지기 제한
		List<Member> clubmembers = clubService.findMemberDetails(clubCode)
				.stream()
				.limit(3)
				.collect(Collectors.toList());
		model.addAttribute("clubmembers", clubmembers);
		
		Map<String, Integer> memberCount = clubService.membercount();
		model.addAttribute("memberCount", memberCount);
		
		model.addAttribute("clubdto", clubDTO);
		
		return "club/club_modify";
	}
	
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/club_remove")
	public String clubRemove(@RequestParam(value = "clubCode", required = false) String clubCode
			, HttpServletRequest request) {
		clubService.remove(clubCode);
		
		return "redirect:/";
	}
	
	@PreAuthorize("hasRole('USER')")	
	@PostMapping("/club_join")
	public String clubJoin(@RequestParam(value = "clubCode", required = false) String clubCode
						 , @RequestParam(value = "clubPw", required = false) String clubPw
						 , Authentication authentication
						 , HttpServletRequest request
						 , RedirectAttributes redirectAttributes) {
		
		String memId = authentication.getName();		
		if (clubService.isJoinMember(memId, clubCode)) {
			redirectAttributes.addFlashAttribute("message", "이미 가입된 회원입니다.");
			return "redirect:/club/club_detail?clubCode=" + clubCode;
		} 
		
		clubService.join(memId, clubCode, clubPw);
		redirectAttributes.addFlashAttribute("message", "클럽에 성공적으로 가입되었습니다.");
		return "redirect:/club/club_detail?clubCode=" + clubCode;
	}
	
	// 회원탈퇴
	@PreAuthorize("hasRole('USER')")	
	@PostMapping("/club_joindelete")
	public String clubJoinDelete(@RequestParam(value = "clubCode", required = false) String clubCode
			, Authentication authentication
			, HttpServletRequest request
			, RedirectAttributes redirectAttributes) {
		String memId = authentication.getName(); // 로그인한 회원ID

		if(clubService.isJoinDeleteMember(memId, clubCode)) {
			redirectAttributes.addFlashAttribute("message", "이미 탈퇴된 회원입니다.");
			return "redirect:/club/club_detail?clubCode=" + clubCode;
		}
		
		clubService.joindelete(memId, clubCode);	
		clubService.removeClubResMember(clubCode, memId);
		redirectAttributes.addFlashAttribute("message", "클럽에 성공적으로 탈퇴되었습니다.");
		return "redirect:/club/club_detail?clubCode=" + clubCode;
			
	}
		
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/club_member")
	public String clubMember(@RequestParam("clubCode") String clubCode
			, PageRequestDTO pageRequestDTO
			, HttpServletRequest request, Model model
			, @AuthenticationPrincipal User user) {
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);

        ClubDTO clubDTO = clubService.detail(clubCode);
        model.addAttribute("clubdto", clubDTO);

        PageResponseDTO<Member> responseDTO = clubService.findMemberAll(clubCode, pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        
        Map<String, Integer> memberCount = clubService.membercount();
        model.addAttribute("memberCount", memberCount);
                
		return "club/club_member"; 
	}

	@GetMapping("/club_board")
	public String clubBoard(@RequestParam("clubCode") String clubCode, PageRequestDTO pageRequestDTO, @RequestParam(value = "type", required = false) String type
			, HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);
        
		log.info("do clubBoardList");
		ClubDTO clubDTO = clubService.detail(clubCode);
        model.addAttribute("clubdto", clubDTO);

		pageRequestDTO.setType(type);

		PageResponseDTO<ClubBoardAllListDTO> boardDTO = clubService.listWithAll(pageRequestDTO, clubCode);
        log.info(boardDTO);
		model.addAttribute("responseDTO", boardDTO);
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		MemberSecurityDTO principal = (MemberSecurityDTO) authentication.getPrincipal();
		model.addAttribute("principal", principal);
		
		String memId = principal.getMemId();
		boolean isMember = clubService.isJoinMember(memId, clubCode);
		model.addAttribute("isMember", isMember);

		return "club/club_board"; 
	}


	@PostMapping("/board_register")
	public String clubBoardPost(@RequestParam("CLUB_CODE") String clubCode
			,HttpServletRequest request, Model model
			,@Valid ClubBoardDTO clubBoardDTO
			, BindingResult bindingResult
			, RedirectAttributes redirectAttributes){
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);
		log.info("registerController");

		log.info(clubBoardDTO);
		int bno = clubService.registerClubBoard(clubBoardDTO);

		log.info(bno);
		return "redirect:/club/club_board?clubCode="+clubCode;
	}
	
	
	@GetMapping("/club_calendar")
	public String clubCalendar(HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);
        
		return "club/club_calendar"; 
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/club_board_write")
	public String clubBoardWrite(HttpServletRequest request, Model model){
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);
        
		return "club/club_board_write";
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping({"/club_board_read"})
	public String clubBoardRead(HttpServletRequest request, Model model, @RequestParam("boardNo") int boardNo, @RequestParam("clubCode") String clubCode) {
		String requestURI = request.getRequestURI();
		log.info("do clubBoardRead");
		ClubBoardDTO clubBoardDTO = clubService.readOne(boardNo, clubCode);
		model.addAttribute("currentURI", requestURI);
		model.addAttribute("dto", clubBoardDTO);
		log.info(clubBoardDTO);

		return "club/club_board_readOne";
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping({"/club_board_modify"})
	public String callClubBoardModify(HttpServletRequest request, Model model, @RequestParam("boardNo") int boardNo, @RequestParam("clubCode") String clubCode) {
		String requestURI = request.getRequestURI();
		log.info("do clubBoardRead");
		ClubBoardDTO clubBoardDTO = clubService.readOne(boardNo, clubCode);
		model.addAttribute("currentURI", requestURI);
		model.addAttribute("dto", clubBoardDTO);
		log.info(clubBoardDTO);

		return "club/club_board_modify";
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/modify_Board")
	public String clubBoardModify(HttpServletRequest request, Model model, @RequestParam(name = "memId", defaultValue = "") String memId,
								@RequestParam(name = "boardNo", defaultValue = "") String boardNo,
								@Valid ClubBoardDTO clubBoardDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		log.info("do boardModify");
		clubBoardDTO.setBOARD_NO(Integer.parseInt(boardNo));
		ClubBoardDTO resultClubBoardDTO = clubService.modifyClubBoard(clubBoardDTO);

		model.addAttribute("currentURI", request.getRequestURI());
		model.addAttribute("dto", resultClubBoardDTO);

		return "club/club_board_readOne";
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/board_remove")
	public String clubBoardRemove(HttpServletRequest request, Model model, @RequestParam(name = "boardNo", defaultValue = "") String boardNo,
								@Valid ClubBoardDTO clubBoardDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		log.info("do boardRemove");
		clubBoardDTO.setBOARD_NO(Integer.parseInt(boardNo));
		String clubCode = clubService.removeClubBoard(clubBoardDTO);

		return "redirect:/club/club_board?clubCode="+clubCode;
	}
	
	// 클럽 회원 신고
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/club_report")
	public String clubReport(@RequestParam(value = "clubCode") String clubCode,
			@RequestParam(value = "memId") String memId, RedirectAttributes redirectAttributes,
			HttpServletResponse response, HttpServletRequest request, Model model
			, @AuthenticationPrincipal MemberSecurityDTO principal
			, @CookieValue(value = "reported", required = false) String reportCookie) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);

		// 쿠키 확인해서 신고 제한 - 아이디당 하루에 한번
		String cookieValue = principal.getMemId() + "_" + memId;
		if (reportCookie != null && reportCookie.contains(cookieValue)) {
			redirectAttributes.addFlashAttribute("message", "하루에 한 번만 신고 할 수 있습니다.");
			return "redirect:/club/club_member?clubCode=" + clubCode;
		}

		// 쿠키에 신고기록 저장
		String newCookieValue = (reportCookie == null ? "" : reportCookie + ",") + cookieValue;
		try {
			String encodeValue = URLEncoder.encode(newCookieValue, StandardCharsets.UTF_8.name());
			Cookie cookie = new Cookie("reported", encodeValue);
			cookie.setMaxAge(24 * 60 * 60); // 24시간 유지
			cookie.setPath("/"); // 모든경로 사용가능
			response.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			clubService.clubReport(memId, clubCode);
			redirectAttributes.addFlashAttribute("message", "신고가 완료되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", "신고 중 오류가 발생했습니다.");
		}

		return "redirect:/club/club_member?clubCode=" + clubCode;
	}


}
