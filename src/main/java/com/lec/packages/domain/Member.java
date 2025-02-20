package com.lec.packages.domain;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "roleSet")
public class Member extends BaseEntity{
    // Id => P.K 명시
    // @Column => 컬럼 정보 셋업
    // length => 컬럼 데이터 길이
    // name => 컬럼 이름
    // columnDefinition => 컬럼 데이터 타입 지정
    // @JoinColumn => 외래키 명시
    
    @Id
    @Column(name = "MEM_ID", length = 100)
    private String memId;

    @Column(name = "MEM_PW", length = 255)
    private String memPw;

    @Column(name = "MEM_NAME", length = 5)
    private String memName;

    @Column(name = "MEM_NICKNAME", length = 10, nullable = false)
    private String memNickname;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MEM_EXERCISE", referencedColumnName = "EXERCISE_CODE")
    private exercise_code_table memExercise;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MEM_CLUB", referencedColumnName = "EXERCISE_CODE")
    private exercise_code_table memClub;
    

    @Column(name = "MEM_PICTURE",length = 255)
    private String memPicture;

    @Column(name = "MEM_INTRODUCTION", columnDefinition = "TEXT")
    private String memIntroduction;

    @Column(name = "MEM_GENDER", columnDefinition = "TINYINT(1)")
    private boolean memGender;

    @Column(name = "MEM_TELL", length = 11)
    private String memTell;
 
    @Column(name = "MEM_EMAIL", length = 30, nullable = false)
    private String memEmail;

    @Column(name = "MEM_BIRTHDAY", length = 30)
    private String memBirthday;

    @Column(name = "MEM_ADDRESS", length = 100)
    private String memAddress;
    
    @Column(name = "MEM_ADDRESS_DETAIL", length = 100)
    private String memAddressDetail;

    @Column(name = "MEM_ZIPCODE", length = 10)
    private String memZipcode;

    @Column(name = "MEM_ADDRESS_SET", length = 100)
    private String memAddressSet;
    
    @Column(name = "MEM_MONEY", columnDefinition = "DECIMAL(50, 0) DEFAULT 0")
    private BigDecimal memMoney;
    
    @Column(name = "MEM_ISMANAGER", columnDefinition = "TINYINT(1)")
    private boolean memIsmanager;

    @Column(name = "DELETE_FLAG", columnDefinition = "TINYINT(1)")
    private boolean deleteFlag;
    
    @Column(name = "MEM_SOCIAL", columnDefinition = "TINYINT(1)")
    private boolean memSocial;
    

    @ElementCollection(fetch = FetchType.LAZY)
	private Set<MemberRole> roleSet = new HashSet<>();
	
    public void changePassword(String memPw) {
		this.memPw = memPw;
	}
	
	public void changeEmail(String memEmail) {
		this.memEmail = memEmail;
	}
	
	public void changeDel(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	
	public void addRole(MemberRole memberRole) {
		this.roleSet.add(memberRole);
	}
	
	public void clearRole() {
		this.roleSet.clear();
	}
	
	public void changeSocial(boolean memSocial) {
		this.memSocial = memSocial;
	}
	
	 public void setMEM_PICTURE(String memPicture) {
	        this.memPicture = memPicture;
	    }

	@OneToMany(mappedBy = "memId", fetch = FetchType.LAZY) 
	private List<Club_Member_List> clubs;
	

	// 엔티티 저장 전에 기본값 설정
	@PrePersist
	public void setDefaultValues() {
	    if (this.memMoney == null) {
	        this.memMoney = BigDecimal.ZERO; // 기본값을 0으로 설정
	    }
	}

	@Transient // DB에는 저장되지않게 설정
	private int reportCount;

    
}
