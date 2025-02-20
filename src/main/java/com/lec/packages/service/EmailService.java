package com.lec.packages.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String tempPassword) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 이메일 수신자 및 제목 설정
            helper.setTo(to);
            helper.setSubject(subject);

            // HTML 템플릿을 로드하여 이메일 본문으로 사용
            String emailContent = loadEmailTemplate(tempPassword);
            helper.setText(emailContent, true); // HTML 전송

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }

    // HTML 이메일 템플릿을 불러오는 메서드
    private String loadEmailTemplate(String tempPassword) {
        try {
            // Spring Boot에서 `resources/templates/` 내부 파일을 불러오기 위해 `ClassPathResource` 사용
            ClassPathResource resource = new ClassPathResource("templates/member/email-template.html");
            log.info("🔍 템플릿 로드 시도: {}", resource.getPath());

            InputStream inputStream = resource.getInputStream();
            String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            log.info("✅ 템플릿 로드 성공");
            return content.replace("{TEMP_PASSWORD}", tempPassword);
        } catch (Exception e) {
            log.error("❌ 이메일 템플릿 로드 실패: {}", e.getMessage(), e);
            return "<h2>FITLINK에서 임시 비밀번호를 보내드립니다.</h2><h3>임시 비밀번호: " + tempPassword + "</h3>"; // 기본 텍스트 반환
        }
    }
}

