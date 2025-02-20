package com.lec.packages.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.Member;
import com.lec.packages.domain.MemberRole;
import com.lec.packages.dto.MemberSecurityDTO;
import com.lec.packages.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("======> User Request: {}", userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();
        log.info("Client Name: {}", clientName);

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> paramMap = oAuth2User.getAttributes();

        // Extract user details
        String email = extractEmail(clientName, paramMap);
        String id = clientName + "_" + email;
        String nickname = extractNickname(clientName, paramMap);
        String picture = extractProfilePicture(clientName, paramMap);
        String phone = extractPhoneNumber(clientName, paramMap);
        boolean gender = extractGender(clientName, paramMap);
        String birthday = extractBirthday(clientName, paramMap);
        String name = extractName(clientName, paramMap);

        MemberSecurityDTO memberSecurityDTO = generateDTO(id, email, nickname, picture, phone, gender, birthday, name, true, false, false);
        log.info("Generated MemberSecurityDTO: {}", memberSecurityDTO);

        return memberSecurityDTO;
    }

    @Transactional
    private MemberSecurityDTO generateDTO(String id, String email, String nickname, String picture, String phone, boolean gender, String birthday, String name, boolean social, boolean manager, boolean delete) {
        Optional<Member> result = memberRepository.findById(id);

        if (result.isEmpty()) {
            // 신규 사용자 생성
            Member newMember = createNewMember(id, email, nickname, picture, phone, gender, birthday, name, social, manager, delete);
            memberRepository.save(newMember);
            return createMemberSecurityDTO(newMember);
        } else {
        	
            // 기존 사용자 정보 업데이트
            Member existingMember = result.get();
            boolean updated = false;

            if (!Objects.equals(existingMember.getMemNickname(), nickname)) {
                existingMember.setMemNickname(nickname);
                updated = true;
            }
            if (!Objects.equals(existingMember.getMemPicture(), picture)) {
                existingMember.setMemPicture(picture);
                updated = true;
            }
            if (!Objects.equals(existingMember.getMemTell(), phone)) {
                existingMember.setMemTell(phone);
                updated = true;
            }
            if (!Objects.equals(existingMember.getMemEmail(), email)) {
                existingMember.setMemEmail(email);
                updated = true;
            }
          
            
            
            if (updated) {
                memberRepository.save(existingMember);
            }

            existingMember.getRoleSet().size(); // 강제 초기화
         

            return createMemberSecurityDTO(existingMember);
        }
    }


    private Member createNewMember(String id, String email, String nickname, String picture, String phone, boolean gender, String birthday, String name, boolean social, boolean manager, boolean delete) {
        Member member = Member.builder()
                .memId(id)
                .memPw(passwordEncoder.encode(UUID.randomUUID().toString()))
                .memEmail(email)
                .memNickname(nickname)
                .memPicture(picture)
                .memTell(phone)
                .memName(name)
                .memGender(gender)
                .memBirthday(birthday)
                .memIsmanager(manager)
                .deleteFlag(delete)
                .memSocial(social)
                .build();

        if (member.getRoleSet() == null) {
            member.setRoleSet(new HashSet<>());
        }
        member.addRole(MemberRole.USER);

        return member;
    }

    private MemberSecurityDTO createMemberSecurityDTO(Member member) {
        log.info("Creating DTO: memSocial={}, deleteFlag={}", member.isMemSocial(), member.isDeleteFlag());
        return new MemberSecurityDTO(
                member.getMemId(),
                member.getMemPw(),
                member.getMemName(),
                member.getMemNickname(),
                member.getMemExercise(),
                member.getMemClub(),
                member.getMemPicture(),
                member.getMemIntroduction(),
                member.isMemGender(),
                member.getMemTell(),
                member.getMemEmail(),
                member.getMemBirthday(),
                member.getMemAddress(),
                member.getMemAddressDetail(),
                member.getMemZipcode(),
                member.getMemAddressSet(),
                member.getMemMoney(),
                member.isMemIsmanager(),
                member.isDeleteFlag(),
                member.isMemSocial(),
                member.getRoleSet()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(String clientName, Map<String, Object> paramMap) {
        if ("kakao".equalsIgnoreCase(clientName)) {
            return extractKakaoAttribute(paramMap, "email");
        } else if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            return (String) response.get("email");
        } else if ("google".equalsIgnoreCase(clientName)) {
            return (String) paramMap.get("email");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(String clientName, Map<String, Object> paramMap) {
        if ("kakao".equalsIgnoreCase(clientName)) {
            return extractKakaoAttribute(paramMap, "nickname");
        } else if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            return (String) response.get("nickname");
        } else if ("google".equalsIgnoreCase(clientName)) {
            return (String) paramMap.get("name");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractProfilePicture(String clientName, Map<String, Object> paramMap) {
        if ("kakao".equalsIgnoreCase(clientName)) {
            return extractKakaoAttribute(paramMap, "profile_image_url");
        } else if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            return (String) response.get("profile_image");
        } else if ("google".equalsIgnoreCase(clientName)) {
            return (String) paramMap.get("picture");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractPhoneNumber(String clientName, Map<String, Object> paramMap) {
        if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            String phone = (String) response.get("mobile");
            return phone != null ? phone.replaceAll("-", "") : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean extractGender(String clientName, Map<String, Object> paramMap) {
        if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            return "M".equalsIgnoreCase((String) response.get("gender"));
        }
        return true; // 기본값: 남성
    }

    @SuppressWarnings("unchecked")
    private String extractBirthday(String clientName, Map<String, Object> paramMap) {
        if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            String birthDay = (String) response.get("birthday");
            String birthYear = (String) response.get("birthyear");
            return birthYear + "-" + birthDay;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractName(String clientName, Map<String, Object> paramMap) {
        if ("naver".equalsIgnoreCase(clientName)) {
            Map<String, Object> response = (Map<String, Object>) paramMap.get("response");
            return (String) response.get("name");
        } else if ("google".equalsIgnoreCase(clientName)) {
            return (String) paramMap.get("name");
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private String extractKakaoAttribute(Map<String, Object> paramMap, String attributeKey) {
        Object value = paramMap.get("kakao_account");
        if (value instanceof LinkedHashMap) {
            LinkedHashMap accountMap = (LinkedHashMap) value;

            if ("email".equals(attributeKey)) {
                return (String) accountMap.get("email");
            } else if ("nickname".equals(attributeKey) || "profile_image_url".equals(attributeKey)) {
                Object profile = accountMap.get("profile");
                if (profile instanceof LinkedHashMap) {
                    LinkedHashMap profileMap = (LinkedHashMap) profile;
                    return (String) profileMap.get(attributeKey);
                }
            }
        }
        log.warn("Attribute '{}' not found in Kakao response", attributeKey);
        return null;
    }
}
