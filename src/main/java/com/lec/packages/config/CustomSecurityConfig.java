package com.lec.packages.config;

import javax.sql.DataSource;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.lec.packages.security.handler.Custom403Handler;
import com.lec.packages.security.handler.CustomAuthenticationSuccessHandler;
import com.lec.packages.security.handler.CustomSocialLoginSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {

	private final DataSource dataSource;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@SuppressWarnings("deprecation")
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("---------------- filterChain -------------------");

		log.info("CustomSecurityConfig 초기화 중...");

		// HTTPS 강제 리디렉션 설정
//        http.requiresChannel(channel -> 
//            channel.anyRequest().requiresSecure()
//        );

		// 인증처리로직
		// http.formLogin(); 스프링시큐티에서 기본제공하는 로그인 화면
		// http.formLogin().loginPage("/member/login");
		http.formLogin(login -> login.loginPage("/member/login") // 로그인 페이지 URL
				// .defaultSuccessUrl("/") // 로그인 성공 후 이동할 URL
				.successHandler(new CustomAuthenticationSuccessHandler()).failureUrl("/member/login?error") // 로그인 실패 시
																											// 이동할 URL
				.usernameParameter("username") // 사용자명 파라미터 이름
				.passwordParameter("password")); // 비밀번호 파라미터 이름

		http.csrf(csrf -> csrf.disable());


		// 로그아웃 설정 추가
		http.logout(logout -> logout.logoutUrl("/member/logout") // 로그아웃 URL
				.invalidateHttpSession(true) // 세션 무효화
				.clearAuthentication(true) // 인증 정보 초기화
				.deleteCookies("JSESSIONID") // 쿠키 삭제
				.logoutSuccessUrl("/")); // 로그아웃 후 이동할 URL

		http.oauth2Login(login -> login.loginPage("/member/login") // 소셜 로그인 진입점 설정
				.successHandler(new CustomSocialLoginSuccessHandler()) // 로그인 성공 시 동적 리다이렉트
				// .defaultSuccessUrl("/")
				.failureUrl("/member/login?error"));

		// 403에러 핸들링
		http.exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler()));

		http.authorizeRequests(auth -> auth
				// 클럽 및 시설 관련 URL은 모두 허용
				.requestMatchers("/club/club_detail", "/club/club_detail**").permitAll()
				.requestMatchers("/club/club_modify").permitAll()
				.requestMatchers("/facility/facility_main", "/facility/main/**", "/facility/search", "/facility/search/**"
								, "/facility/privatesearch", "/facility/privatesearch/**").permitAll()
				// 관리자 전용 URL
				.requestMatchers(HttpMethod.DELETE, "/admin/remove/**").hasRole("ADMIN").requestMatchers("/admin/**")
				.hasRole("ADMIN")
				// 로그인 및 정적 리소스는 모두 허용
				.requestMatchers("/member/login","/member/checkId","/member/find_pw", "/member/join", "/css/**", "/js/**", "/img/**", "/view/**","/")
				.permitAll()
				.requestMatchers(HttpMethod.DELETE, "/planner/delete").permitAll()
				.requestMatchers("/api/weather**").permitAll()
				// 나머지 요청은 인증된 사용자만 접근 가능
				.anyRequest().authenticated());

		http.requestCache(requestCache -> requestCache
			    .requestCache(new ConditionalRequestCache())
			);

		return http.build();

	}

	// Custom RequestCache 적용
	public class ConditionalRequestCache implements RequestCache {

	    private final HttpSessionRequestCache defaultCache = new HttpSessionRequestCache();
	    private final NullRequestCache nullCache = new NullRequestCache();

	    @Override
	    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
	        String requestUri = request.getRequestURI();

	        // "/view/**" 및 "/member/checkId" 경로는 NullRequestCache 사용
	        if (isExcludedPath(requestUri)) {
	            nullCache.saveRequest(request, response);
	        } else {
	            defaultCache.saveRequest(request, response);
	        }
	    }

	    @Override
	    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
	        String requestUri = request.getRequestURI();

	        if (isExcludedPath(requestUri)) {
	            return nullCache.getRequest(request, response);
	        } else {
	            return defaultCache.getRequest(request, response);
	        }
	    }

	    @Override
	    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
	        String requestUri = request.getRequestURI();

	        if (isExcludedPath(requestUri)) {
	            nullCache.removeRequest(request, response);
	        } else {
	            defaultCache.removeRequest(request, response);
	        }
	    }

	    @Override
	    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
	        String requestUri = request.getRequestURI();

	        if (isExcludedPath(requestUri)) {
	            return nullCache.getMatchingRequest(request, response);
	        } else {
	            return defaultCache.getMatchingRequest(request, response);
	        }
	    }

	    /**
	     * 특정 경로를 제외할 조건을 정의합니다.
	     *
	     * @param requestUri 요청 URI
	     * @return 제외 경로에 해당하면 true
	     */
	    private boolean isExcludedPath(String requestUri) {
	        return requestUri.startsWith("/view/") || requestUri.equals("/member/checkId");
	    }
	}

	
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		log.info("CustomSocialLoginSuccessHandler 등록");
		return new CustomSocialLoginSuccessHandler();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new Custom403Handler();
	}

	// static resource(css, js...)에 접근할 수 있도록 spring security에서 제외
//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//		log.info("--------------- WebSecurityCustomizer -------------------");
//		return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
		repo.setDataSource(dataSource);
		return repo;
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
