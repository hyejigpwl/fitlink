package com.lec.packages.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/*
 403(Forbidden) 에러 : 서버에서 사용자의 요청을 거부했다는 의미
 스프링시큐리티에서 @PreAuthorize("isAuthentication")인 경우 사용자가 로그인이
 안되었다면 302메세지와 함께 로그인경로로 이동하지만 403에러는 에러가 발생된다.
 이 에러페이지를 상솽에 맞게 처리하기 위해서 AccessDeniedHandler을 구현할 객체를
 작성해서 처리하는 클래스(403에러는 여러 종류가 있다. 예를 들어 사용자권한이 안맞을
 경우 또는 특정조건이 맞지 않을 경우 등...)
 */

@Log4j2
public class Custom403Handler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse res,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		log.info("---------------------");
		res.setStatus(HttpStatus.FORBIDDEN.value());
		
		// JSON요청이었는지 확인
		String contentType = req.getHeader("Content-Type");
		boolean jsonRequest = contentType.startsWith("application/json");
		log.info("JSON데이터 요청 여부 = " + jsonRequest);
		
		// 일반 요청일 경우
		if(!jsonRequest) {
			res.sendRedirect("/member/login?error=ACCESS_DENIED");
		}
	}

}
