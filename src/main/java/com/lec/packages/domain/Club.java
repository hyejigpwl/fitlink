package com.lec.packages.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
@ToString
public class Club extends BaseEntity{
    // Id => P.K 명시
    // @Column => 컬럼 정보 셋업
    // length => 컬럼 데이터 길이
    // name => 컬럼 이름
    // columnDefinition => 컬럼 데이터 타입 지정
    // @JoinColumn => 외래키 명시

    @Id
    @Column(length = 10, name = "CLUB_CODE")
    private String clubCode;

    @Column(nullable = false, length = 20, name = "CLUB_NAME")
    private String clubName;
    
    @JoinColumn(name = "EXERCISE_CODE")
    @Column(length = 20, name = "CLUB_EXERCISE")
    private String clubExercise;

    @Column(nullable = false, columnDefinition = "TEXT", name = "CLUB_INTRODUCTION")
    private String clubIntroduction;

    @Column(length = 100, name = "CLUB_THEME")
    private String clubTheme;

    @Column(name = "CLUB_IMAGE_1")
    private String clubImage1;
    
    @Column(name = "CLUB_IMAGE_2")
    private String clubImage2;

    @Column(name = "CLUB_IMAGE_3")
    private String clubImage3;

    @Column(name = "CLUB_IMAGE_4")
    private String clubImage4;
    
    @Column(length = 100, name = "CLUB_ADDRESS")
    private String clubAddress;

    @JoinColumn(name = "MEM_ID")
    @Column(nullable = false, length = 100, name = "MEM_ID")
    private String memId;

    @Column(name = "CLUB_ISPRIVATE")
    private boolean clubIsprivate;
    
    @Column(length = 30, name = "CLUB_PW")
    private String clubPw;

    @Column(name = "DELETE_FLAG")
    private boolean deleteFlag;

	public void change(String clubIntroduction, String clubAddress, String clubName
					 , String clubTheme, String clubExercise, String clubPw
					 , boolean clubIsprivate, String clubImage1, String clubImage2
					 , String clubImage3, String clubImage4) {
		this.clubIntroduction = clubIntroduction;
		this.clubAddress = clubAddress;
		this.clubName = clubName;
		this.clubTheme = clubTheme;
		this.clubExercise = clubExercise;		
		this.clubPw = clubPw;
		this.clubIsprivate = clubIsprivate;
		this.clubImage1 = clubImage1;
	    this.clubImage2 = clubImage2; 
	    this.clubImage3 = clubImage3; 
	    this.clubImage4 = clubImage4; 
	}

	public void remove(boolean deleteFlag) {
		this.deleteFlag = true;		
	}
	
	@OneToMany(mappedBy = "clubCode", fetch = FetchType.LAZY)
	private List<Club_Member_List> members;

}
