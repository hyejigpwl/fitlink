package com.lec.packages.util;

import java.util.Random;

import com.lec.packages.repository.FacilityRepository;

public class RandomStringGenerator {
	
	/**
	 * FACILITY_CODE 자동 생성 -> "FACIL_xxxx"
     * 지정된 길이의 랜덤 문자열을 생성합니다.
     * @param length 생성할 문자열의 길이
     * @param facilityRepository 데이터베이스 중복확인을 위한 Repository
     * @return 고유한 랜덤 문자열
     */
    public static String generateRandomString(int length, FacilityRepository facilityRepository) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 사용 가능한 문자 집합
  
        Random random = new Random();
        String randomString;
        
        do {
        	StringBuilder sb = new StringBuilder();
        	
        	for(int i=0; i<length; i++) {
        		int index = random.nextInt(characters.length());
        		sb.append(characters.charAt(index));
        	}
        	randomString = sb.toString();
        }while(facilityRepository.existsByFacilityCode("FACIL_"+randomString));

        return randomString;
    }
}
