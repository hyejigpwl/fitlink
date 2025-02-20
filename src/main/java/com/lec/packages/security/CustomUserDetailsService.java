package com.lec.packages.security;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lec.packages.domain.Member;
import com.lec.packages.dto.MemberSecurityDTO;
import com.lec.packages.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
//	private PasswordEncoder passwordEncoder;
	
//	public CutomUserDetailsService() {
//		this.passwordEncoder = new BCryptPasswordEncoder();
//	}
//	
//	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		
//		log.info("--------------> loadUserByUsername ..... " + username);
//		
//		UserDetails userDetails = User.builder()
//				.username("user1")
//				// .password("{noop}12345")
//				.password(passwordEncoder.encode("12345"))
//				.authorities("ROLE_USER")
//				.build();
//		
//		return userDetails;
//	}

	private final MemberRepository memberRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		log.info("--------------> loadUserByUsername ..... " + username);
		
		Optional<Member> result = memberRepository.getWithRoles(username);
		
		if(result.isEmpty()) { // 해당되는 ID를 가진 사용자가 없다면
			throw new UsernameNotFoundException(String.format("UserName(%s)을 찾지 못했습니다!", username));
		}
		
		Member member = result.get();
		
		// DELETE_FLAG가 true(1)인 경우 로그인 차단
	    if (member.isDeleteFlag()) {
	        throw new UsernameNotFoundException("이 계정은 비활성화 상태입니다. 관리자에게 문의하세요.");
	    }
		
		MemberSecurityDTO memberSecurityDTO =
				new MemberSecurityDTO(
						member.getMemId()
					  , member.getMemPw()
					  , member.getMemName()
					  , member.getMemNickname()
					  , member.getMemExercise()
					  , member.getMemClub()
					  , member.getMemPicture()
					  , member.getMemIntroduction()
					  , member.isMemGender()
					  , member.getMemTell()
					  , member.getMemEmail()
					  , member.getMemBirthday()
					  , member.getMemAddress()
					  , member.getMemAddressDetail()
					  , member.getMemZipcode()
					  , member.getMemAddressSet()
					  , member.getMemMoney()
					  , member.isMemIsmanager()
					  , member.isDeleteFlag()
					  , member.isMemSocial()
					  , member.getRoleSet()
					          .stream()
					          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
					          .collect(Collectors.toList())
					);
		
		log.info("memberSecurityDTO ==> " + memberSecurityDTO);
		
		return memberSecurityDTO;
	}
	
}
