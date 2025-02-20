package com.lec.packages.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lec.packages.domain.Member;
import com.lec.packages.dto.ClubDTO;
import com.lec.packages.dto.MemberSecurityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.service.ClubService;
import com.lec.packages.service.KakaoApiService;
import com.lec.packages.service.MemberService;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MainController {

    private final ClubService clubService;
    private final MemberRepository memberRepository;
    private final KakaoApiService kakaoApiService;
    private final MemberService memberService;

   
    @GetMapping("/")
    public String mainClub(PageRequestDTO pageRequestDTO, HttpServletRequest request, Model model,
                           @RequestParam(value = "theme", required = false, defaultValue = "ALL") String clubTheme,
                           @RequestParam(value = "address", required = false, defaultValue = "ALL") String address) {
    	
    	// 현재 URI 추가
        String currentURI = request.getRequestURI();
        model.addAttribute("currentURI", currentURI != null ? currentURI : "");
    	
    	 // 현재 사용자 가져오기
        Member member = getAuthenticatedMember();
        if (member != null) {
            model.addAttribute("member", member); // Member 객체를 모델에 추가

            if ("ALL".equalsIgnoreCase(address)) {
                if (member.getMemAddressSet() != null && !member.getMemAddressSet().isEmpty()) {
                    address = member.getMemAddressSet();
                } else if (member.getMemAddress() != null){
                	address = member.getMemAddress();
                }
            }
        }
        
        address = "ALL".equalsIgnoreCase(address) ? null : address;
        clubTheme = "ALL".equalsIgnoreCase(clubTheme) ? null : clubTheme;

        // 지역 정보 추가
        try {
            String region = kakaoApiService.extractRegionFromAuthenticatedMember();
            model.addAttribute("region", region); // 구/동/면 값을 모델에 추가
        } catch (Exception e) {
            model.addAttribute("region", "지역 정보를 가져올 수 없습니다."); // 예외 처리
            e.printStackTrace();
        }


        // 클럽 목록 필터링
        PageResponseDTO<ClubDTO> responseDTO;
        if (address == null && clubTheme == null) {
            responseDTO = clubService.list(pageRequestDTO); // 모든 클럽 조회
        } else if (address == null) {
            responseDTO = clubService.listByTheme(pageRequestDTO, clubTheme); // 테마 필터링
        } else {
            responseDTO = clubService.listByAddressAndTheme(pageRequestDTO, address, clubTheme); // 주소 및 테마 필터링
        }

        // 모델에 데이터 추가
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("theme", clubTheme);
        model.addAttribute("address", address);
        
        log.info("====MainController : {}, {}", clubTheme, address);

        return "index";
    }
    
    /*
    @GetMapping("/")
    public String mainClub(PageRequestDTO pageRequestDTO, HttpServletRequest request, Model model,
                           @RequestParam(value = "theme", required = false) String clubTheme,
                           @RequestParam(value = "address", required = false, defaultValue = "ALL") String address) {
    	
    	// 현재 URI 추가
        String currentURI = request.getRequestURI();
        model.addAttribute("currentURI", currentURI);

    	 // 현재 사용자 가져오기
        Member member = getAuthenticatedMember();
        if (member != null) {
            model.addAttribute("member", member); // Member 객체를 모델에 추가
            
            if ("ALL".equalsIgnoreCase(address)) {
                if (member.getMemAddressSet() != null && !member.getMemAddressSet().isEmpty()) {
                    address = member.getMemAddressSet();
                }
            }      
        } 
        
        // 지역 정보 추가
        try {
            String region = kakaoApiService.extractRegionFromAuthenticatedMember();
            model.addAttribute("region", region); // 구/동/면 값을 모델에 추가
        } catch (Exception e) {
            model.addAttribute("region", "지역 정보를 가져올 수 없습니다."); // 예외 처리
            e.printStackTrace();
        }

        if (clubTheme == null || clubTheme.isEmpty()) {
            clubTheme = "ALL";
        }

        PageResponseDTO<ClubDTO> responseDTO = "ALL".equals(clubTheme) ? clubService.list(pageRequestDTO)
                : clubService.listByTheme(pageRequestDTO, clubTheme);

        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("theme", clubTheme);
        model.addAttribute("address", address);

        return "index";
    }

    @GetMapping("/clubByaddress")
    public String clubsByAddressAndTheme(PageRequestDTO pageRequestDTO,
                                         @RequestParam(value = "theme", required = false, defaultValue = "ALL") String clubTheme,
                                         @RequestParam(value = "address", required = false, defaultValue = "ALL") String address,
                                         HttpServletRequest request, Model model) {
    	
        String currentURI = request.getRequestURI();
        model.addAttribute("currentURI", currentURI);
        
        // 현재 사용자 가져오기
        Member member = getAuthenticatedMember();
        if (member != null) {
            model.addAttribute("member", member); // Member 객체를 모델에 추가
            
            if ("ALL".equalsIgnoreCase(address) && member.getMemAddressSet() != null) {
                address = member.getMemAddressSet();
            }        
        }            
        model.addAttribute("address", address);
        
        // 주소와 테마에 따라 클럽 목록 필터링
        PageResponseDTO<ClubDTO> responseDTO;
        if ("ALL".equalsIgnoreCase(address) && "ALL".equalsIgnoreCase(clubTheme)) {
            responseDTO = clubService.list(pageRequestDTO); // 모든 클럽 조회
        } else if ("ALL".equalsIgnoreCase(address)) {
            responseDTO = clubService.listByTheme(pageRequestDTO, clubTheme); // 테마 필터링
        } else {
            responseDTO = clubService.listByAddressAndTheme(pageRequestDTO, address, clubTheme); // 주소 및 테마 필터링
        }

        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("theme", clubTheme);

        return "index";
    } 
     */
    
    private Member getAuthenticatedMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MemberSecurityDTO) {
            MemberSecurityDTO memberDTO = (MemberSecurityDTO) authentication.getPrincipal();
            Optional<Member> memberOptional = memberRepository.findById(memberDTO.getMemId());
            return memberOptional.orElse(null); // DB에서 Member 객체 조회
        }
        
        return null; // 인증되지 않은 사용자일 경우 null 반환
    }
    
    @PostMapping("/update-mem-address")
    @ResponseBody
    public ResponseEntity<?> updateMemAddressSet(@RequestBody Map<String, String> payload) {
        String memAddressSet = payload.get("memAddressSet");

        // 현재 로그인 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberSecurityDTO)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }        
        
        MemberSecurityDTO member = (MemberSecurityDTO) authentication.getPrincipal();
        member.setMemAddressSet(memAddressSet); // memSocial 업데이트

        // 실제 데이터베이스 업데이트 로직 호출
        memberService.updateMemAddressSet(member.getMemId(), memAddressSet);

        return ResponseEntity.ok(memAddressSet);
    }

}
