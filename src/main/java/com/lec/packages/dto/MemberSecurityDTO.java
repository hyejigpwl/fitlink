package com.lec.packages.dto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.lec.packages.domain.exercise_code_table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberSecurityDTO extends User implements OAuth2User {

    private static final long serialVersionUID = 1L; // 직렬화 경고 해결

    // 추가 필드
    private String memId;
    private String memPw;
    private String memName;
    private String memNickname;
    private exercise_code_table memExercise;
    private exercise_code_table memClub;
    private String memPicture;
    private String memIntroduction;
    private boolean memGender;
    private String memTell;
    private String memEmail;
    private String memBirthday;
    private String memAddress;
    private String memAddressDetail;
    private String memZipcode;
    private String memAddressSet;
    private BigDecimal memMoney;
    private boolean memIsmanager;
    private boolean deleteFlag;
    private boolean memSocial;

    private Map<String, Object> props; // SSN(카카오) 로그인 정보

    // 생성자
    public MemberSecurityDTO(String username, String password, String memName, String memNickname,
                             exercise_code_table memExercise, exercise_code_table memClub, String memPicture,
                             String memIntroduction, boolean memGender, String memTell, String memEmail,
                             String memBirthday, String memAddress, String memAddressDetail, String memZipcode,
                             String memAddressSet, BigDecimal memMoney, boolean memIsmanager, boolean deleteFlag, boolean memSocial,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);

        this.memId = username; // username은 MEM_ID로 설정
        this.memPw = password; // password는 MEM_PW로 설정
        this.memName = memName;
        this.memNickname = memNickname;
        this.memExercise = memExercise;
        this.memClub = memClub;
        this.memPicture = memPicture;
        this.memIntroduction = memIntroduction;
        this.memGender = memGender;
        this.memTell = memTell;
        this.memEmail = memEmail;
        this.memBirthday = memBirthday;
        this.memAddress = memAddress;
        this.memAddressDetail = memAddressDetail;
        this.memZipcode = memZipcode;
        this.memAddressSet = memAddressSet;
        this.memMoney = memMoney;
        this.memIsmanager = memIsmanager;
        this.deleteFlag = deleteFlag;
        this.memSocial = memSocial;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.props;
    }

    @Override
    public String getName() {
        return this.memId;
    }
    
}
