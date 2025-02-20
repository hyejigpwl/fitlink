package com.lec.packages.dto;

import java.math.BigDecimal;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {
    
	private int number; // 넘버링 필드 추가
    private String facilityCode;

    @NotNull
    private String facilityName;

    @NotNull
    private String facilityAddress;

    private String facilityAddressDetail;
    
    @NotNull
    private String facilityZipcode;
    
    private String facilityDescription;

    @NotNull
    private String facilityImage1;
    
    private String  facilityImage2;
    
    private String  facilityImage3;
    
    private String  facilityImage4;

    private String exerciseCode;
    
    private String exerciseName;

    private boolean facilityIsOnlyClub;

    @NotNull
    private String memId;

    @NotNull
    private BigDecimal price;

    @NotNull(message = "시설 시작 시간은 필수 입력 항목입니다.")
    private LocalTime facilityStartTime;

    @NotNull(message = "시설 종료 시간은 필수 입력 항목입니다.")
    private LocalTime facilityEndTime;
    
    
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    private boolean deleteFlag;
    
    //PRICE 포멧팅
    public String getFormattedPrice() {
        if (price == null) {
            return "0";
        }
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price);
    }
    //createDate 포멧팅
    public String getFormattedCreateDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
        return createDate != null ? createDate.format(formatter) : "";
    }
    
	private BigDecimal facilityLat;
	private BigDecimal facilityLongt;
	
	public FacilityDTO(int number, String facilityCode, String facilityAddress, String facilityAddressDetail
						,String facilityZipCode, String facilityDescription
						, String facilityImage1, String facilityImage2
						, String facilityImage3, String facilityImage4
						, String exerciseCode, String exerciseName
						, boolean facilityIsOnlyClub, String memId
						, BigDecimal price, LocalDateTime createDate
						,LocalDateTime modifyDate, boolean deleteFlag
						,BigDecimal facilityLat, BigDecimal facilityLongt) {
		
		this.number=number;
		this.facilityCode=facilityCode;
		this.facilityAddress=facilityAddress;
		this.facilityAddressDetail=facilityAddressDetail;
		this.facilityZipcode=facilityZipCode;
		this.facilityDescription=facilityDescription;
		this.facilityImage1=facilityImage1;
		this.facilityImage2=facilityImage2;
		this.facilityImage3=facilityImage3;
		this.facilityImage4=facilityImage4;
		this.exerciseCode=exerciseCode;
		this.exerciseName=exerciseName;
		this.facilityIsOnlyClub=facilityIsOnlyClub;
		this.memId=memId;
		this.price=price;
		this.createDate=createDate;
		this.modifyDate=modifyDate;
		this.deleteFlag=deleteFlag;
		this.facilityLat=facilityLat;
		this.facilityLongt=facilityLongt;
		
	}
	
	
    
}
