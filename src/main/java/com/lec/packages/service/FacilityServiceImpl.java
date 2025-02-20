package com.lec.packages.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.lec.packages.domain.Facility;
import com.lec.packages.domain.Member;
import com.lec.packages.domain.Member_Planner;
import com.lec.packages.domain.Reservation;
import com.lec.packages.domain.TransferHistory;
import com.lec.packages.dto.FacilityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.ReservationDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.FacilityRepository;
import com.lec.packages.repository.MemberPlannerRepository;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.repository.ReservationRepository;
import com.lec.packages.repository.TransferHistoryRepository;
import com.lec.packages.util.RandomStringGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class FacilityServiceImpl implements FacilityService{


	 @Value("${kakao.rest.api.key}")
	 private String kakaoApiKey;
	 
	private final ModelMapper modelMapper;

	private final FacilityRepository facilityRepository;

	private final MemberRepository memberRepository;
	
	private final MemberPlannerRepository memberPlannerRepository;
	
	private final ReservationRepository reservationRepository;
	
	private final TransferHistoryRepository transferHistoryRepository;
	

	//시설 등록
	@Transactional
	@Override
	public String register(FacilityDTO facilityDTO) {
		
		Facility facility = modelMapper.map(facilityDTO, Facility.class);
		//고유한 FacilityCode 생성
        String uniqueFacilityCode = RandomStringGenerator.generateRandomString(8,facilityRepository); // 8자리 랜덤 문자열
        facility.setFacilityCode("FACIL_"+uniqueFacilityCode);
        
        // Kakao 지도 API를 사용해 위경도 가져오기
        try {
            String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
            String query = URLEncoder.encode(facilityDTO.getFacilityAddress(), "UTF-8");
            URL url = new URL(apiUrl + "?query=" + query);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

            if (connection.getResponseCode() == 200) { // HTTP OK
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray documents = jsonObject.getJSONArray("documents");

                if (documents.length() > 0) {
                    JSONObject addressData = documents.getJSONObject(0);
                    double lat = addressData.getDouble("y"); // 위도
                    double lon = addressData.getDouble("x"); // 경도

                    facility.setFacilityLat(BigDecimal.valueOf(lat)); // 위도 저장
                    facility.setFacilityLongt(BigDecimal.valueOf(lon)); // 경도 저장
                }
            } else {
                throw new RuntimeException("Geocoding 실패: HTTP " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Geocoding 중 오류 발생.");
        }

       	
        // 파일 경로 설정 (DTO에서 이미 설정된 경로를 사용)
        facility.setFacilityImage1(facilityDTO.getFacilityImage1());
        facility.setFacilityImage2(facilityDTO.getFacilityImage2());
        facility.setFacilityImage3(facilityDTO.getFacilityImage3());
        facility.setFacilityImage4(facilityDTO.getFacilityImage4());

        //엔티티를 데이터베이스에 저장
        facilityRepository.save(facility);
        
        return facility.getFacilityCode();

	}

	
	//유저별 시설 목록 보기
	@Override
	public PageResponseDTO<FacilityDTO> listByUser(String memId, PageRequestDTO pageRequestDTO){
		
		Pageable pageable = pageRequestDTO.getPageable("facilityCode");
		Page<Facility> result = facilityRepository.searchByUser(memId, pageable);
		
		List<FacilityDTO> dtoList = result.getContent()
										  .stream()
										  .map(facility->modelMapper.map(facility, FacilityDTO.class))
										  .collect(Collectors.toList());
		
		return PageResponseDTO.<FacilityDTO>withAll()
				   			  .pageRequestDTO(pageRequestDTO)
				   			  .dtoList(dtoList)
				   			  .total(result.getTotalPages())
				   			  .build();
	}

	// 시설목록 
	@Override
	public PageResponseDTO<FacilityDTO> listAllFacility(PageRequestDTO pageRequestDTO
				,String facilityAddress, String exerciseCode, Boolean facilityIsOnlyClub) {		
		Pageable pageable = pageRequestDTO.getPageable("facilityCode");
		
        /* 검색 필터링
        String[] types = {"a", "e", "c", "ae","aec"};
        String[] keywords = new String[3];
        keywords[0] = (facilityAddress != null) ? facilityAddress : "ALL";
        keywords[1] = (exerciseCode != null) ? exerciseCode : "ALL";
        keywords[2] = (facilityIsOnlyClub == null) ? null : (facilityIsOnlyClub ? "true" : "false");
        */
        
	//	Page<Facility> result = facilityRepository.searchAllImpl(types, keywords, pageable);
		Page<Facility> result = facilityRepository.searchAll(facilityAddress, exerciseCode, facilityIsOnlyClub, pageable);
		List<FacilityDTO> dtoList = result.getContent()
				  .stream()
				  .distinct() // 중복 제거
				  .map(facility -> modelMapper.map(facility, FacilityDTO.class))
				  .collect(Collectors.toList());
		
//		log.info("=== Facility Keywords==== : {}, {}, {}", keywords[0], keywords[1], keywords[2]); 
		log.info("=== Facility Keywords==== : {}, {}, {}", facilityAddress, exerciseCode, facilityIsOnlyClub); 
		
		return PageResponseDTO.<FacilityDTO>withAll()
				.pageRequestDTO(pageRequestDTO)
				.dtoList(dtoList)
				.total((int)result.getTotalElements())
				.build();
	}
	
	
	// 사설시설 보기
	@Override
	public PageResponseDTO<FacilityDTO> listPrivateFacility(PageRequestDTO pageRequestDTO
				,String facilityAddress, String exerciseCode, Boolean facilityIsOnlyClub) {		
		Pageable pageable = pageRequestDTO.getPageable("facilityCode");
		
		Page<Facility> result = facilityRepository.searchPrivate(facilityAddress, exerciseCode, facilityIsOnlyClub, pageable);
		List<FacilityDTO> dtoList = result.getContent()
				  .stream()
				  .distinct() // 중복 제거
				  .map(facility -> modelMapper.map(facility, FacilityDTO.class))
				  .collect(Collectors.toList());
		
		log.info("=== Facility Keywords==== : {}, {}, {}", facilityAddress, exerciseCode, facilityIsOnlyClub); 
		
		return PageResponseDTO.<FacilityDTO>withAll()
				.pageRequestDTO(pageRequestDTO)
				.dtoList(dtoList)
				.total((int)result.getTotalElements())
				.build();
	}
	
	// 공공시설 보기
		@Override
		public PageResponseDTO<FacilityDTO> listPublicFacility(PageRequestDTO pageRequestDTO
					,String facilityAddress, String exerciseCode, Boolean facilityIsOnlyClub) {		
			Pageable pageable = pageRequestDTO.getPageable("facilityCode");
			
			Page<Facility> result = facilityRepository.searchPublic(facilityAddress, exerciseCode, facilityIsOnlyClub, pageable);
			List<FacilityDTO> dtoList = result.getContent()
					  .stream()
					  .distinct() // 중복 제거
					  .map(facility -> modelMapper.map(facility, FacilityDTO.class))
					  .collect(Collectors.toList());

			log.info("=== Facility Keywords==== : {}, {}, {}", facilityAddress, exerciseCode, facilityIsOnlyClub); 
			
			return PageResponseDTO.<FacilityDTO>withAll()
					.pageRequestDTO(pageRequestDTO)
					.dtoList(dtoList)
					.total((int)result.getTotalElements())
					.build();
		}




	//시설 상세보기 
	@Override
	public FacilityDTO getFacilityByCode(String facilityCode) {
		
		Facility facility = 
				facilityRepository.findByFacilityCode(facilityCode)
													.orElseThrow(()->new IllegalArgumentException("시설을 찾을 수 없습니다: "+facilityCode));
		
		 // ModelMapper를 사용하여 Facility -> FacilityDTO 변환
		return modelMapper.map(facility, FacilityDTO.class);
	}

	//시설수정하기
	@Override
	public void modify(FacilityDTO facilityDTO) {
		
		Optional<Facility> result = facilityRepository.findByFacilityCode(facilityDTO.getFacilityCode());
		Facility facility = result.orElseThrow();
		
		 // Kakao 지도 API를 사용해 위경도 가져오기
        try {
            String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
            String query = URLEncoder.encode(facilityDTO.getFacilityAddress(), "UTF-8");
            URL url = new URL(apiUrl + "?query=" + query);

            System.out.println("Kakao API 요청 URL: " + apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

            System.out.println("Kakao API 응답 코드: " + connection.getResponseCode());
            
            if (connection.getResponseCode() == 200) { // HTTP OK
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // 응답 데이터 출력
                System.out.println("Kakao API 응답 데이터: " + response.toString());

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray documents = jsonObject.getJSONArray("documents");

                if (documents.length() > 0) {
                    JSONObject addressData = documents.getJSONObject(0);
                    double lat = addressData.getDouble("y"); // 위도
                    double lon = addressData.getDouble("x"); // 경도

                    facilityDTO.setFacilityLat(BigDecimal.valueOf(lat)); // 위도 저장
                    facilityDTO.setFacilityLongt(BigDecimal.valueOf(lon)); // 경도 저장
                }
            } else {
                throw new RuntimeException("Geocoding 실패: HTTP " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Geocoding 중 오류 발생.");
        }

		 
	    if (facilityDTO.getFacilityImage1() == null) facilityDTO.setFacilityImage1(facility.getFacilityImage1());
	    if (facilityDTO.getFacilityImage2() == null) facilityDTO.setFacilityImage2(facility.getFacilityImage2());
	    if (facilityDTO.getFacilityImage3() == null) facilityDTO.setFacilityImage3(facility.getFacilityImage3());
	    if (facilityDTO.getFacilityImage4() == null) facilityDTO.setFacilityImage4(facility.getFacilityImage4());

	    System.out.println("Before modifyFacility: Lat = " + facilityDTO.getFacilityLat() + ", Longt = " + facilityDTO.getFacilityLongt());
	    
		facility.modifyFacility(facilityDTO.getFacilityName()
								,facilityDTO.getFacilityAddress()
								,facilityDTO.getFacilityAddressDetail()
								,facilityDTO.getFacilityZipcode()
							    ,facilityDTO.getFacilityDescription()
							    ,facilityDTO.isFacilityIsOnlyClub()
							    ,facilityDTO.getPrice()
							    ,facilityDTO.getFacilityStartTime()
							    ,facilityDTO.getFacilityEndTime()
							    ,facilityDTO.getExerciseCode()
							    ,facilityDTO.getFacilityImage1()
							    ,facilityDTO.getFacilityImage2()
							    ,facilityDTO.getFacilityImage3()
							    ,facilityDTO.getFacilityImage4()
							    ,facilityDTO.getFacilityLat()
							    ,facilityDTO.getFacilityLongt());
		
		facilityRepository.save(facility);
		System.out.println("DB 저장 완료: " + facility.getFacilityLat() + ", " + facility.getFacilityLongt());
		
	}
	
	@Override
	public void remove(String facilityCode) {
		Facility facility = facilityRepository.findByFacilityCode(facilityCode).orElseThrow(()->new IllegalArgumentException("존재하지 않는 시설입니다."));
		
		facility.setDeleteFlag(true);
		
		facilityRepository.save(facility);
		
	}
	
	// 시설예약
	@Override
	@Transactional
	public void bookByMember(TransferHistoryDTO transferHistoryDTO, ReservationDTO reservationDTO, BigDecimal memMoney) {
	    // Step 1: 예약 정보를 검증
	    if (reservationDTO.getReservationStartTime().isAfter(reservationDTO.getReservationEndTime())) {
	        throw new IllegalArgumentException("예약 시작 시간이 종료 시간보다 늦을 수 없습니다.");
	    }

	    // Step 2: 예약 시간 차이를 계산하여 총 가격 설정
	    long hours = java.time.Duration.between(
	            reservationDTO.getReservationStartTime(),
	            reservationDTO.getReservationEndTime()
	    ).toHours();
	    BigDecimal totalPrice = BigDecimal.valueOf(hours).multiply(reservationDTO.getPrice());

	    // Step 3: ReservationCode 생성
	    String reservationCode = "" + System.currentTimeMillis();
	    reservationDTO.setReservationCode(reservationCode);
	    transferHistoryDTO.setTransferCode(reservationCode);
	    
	    
	    String payCode = UUID.randomUUID().toString();
	    reservationDTO.setPayCode(payCode);
	    transferHistoryDTO.setPayCode(payCode);

	    // Step 4: ReservationDTO를 Reservation 엔티티로 변환
		Reservation reservation;
		if(reservationDTO.getClubCode() == null || reservationDTO.getClubCode().equalsIgnoreCase("")) {
			reservation = Reservation.builder()
					.reservationCode(reservationCode) // 고유 예약 코드 설정
					.payCode(payCode)
					.facilityCode(reservationDTO.getFacilityCode())
					.facilityName(reservationDTO.getFacilityName())
					.memId(reservationDTO.getMemId())
					.reservationStartTime(reservationDTO.getReservationStartTime())
					.reservationEndTime(reservationDTO.getReservationEndTime())
					.reservationDate(reservationDTO.getReservationDate())
					.count(reservationDTO.getCount())
					.price(totalPrice)
					.memo("관리자의 승인 대기 중")
					.reservationProgress("예약진행중") // 초기 상태 설정
					.deleteFlag(false) // 초기 상태 설정
					.build();
			log.info(reservationCode);
		} else {
			reservation = Reservation.builder()
					.reservationCode(reservationCode) // 고유 예약 코드 설정
					.payCode(payCode)
					.facilityCode(reservationDTO.getFacilityCode())
					.facilityName(reservationDTO.getFacilityName())
					.memId(reservationDTO.getMemId())
					.reservationStartTime(reservationDTO.getReservationStartTime())
					.reservationEndTime(reservationDTO.getReservationEndTime())
					.reservationDate(reservationDTO.getReservationDate())
					.count(reservationDTO.getCount())
					.price(totalPrice)
					.memo("관리자의 승인 대기 중")
					.reservationProgress("예약진행중") // 초기 상태 설정
					.deleteFlag(false) // 초기 상태 설정
					.clubCode(reservationDTO.getClubCode())
					.build();
		}

		TransferHistory transferHistory;
		if (reservationDTO.getClubCode() == null || reservationDTO.getClubCode().equalsIgnoreCase("")) {
		    transferHistory = TransferHistory.builder()
		            .transferCode(reservationCode)
		            .payCode(payCode)
		            .amount(totalPrice)
		            .memo("시설예약")
		            .status("송금성공")
		            .transferDate(LocalDateTime.now())
		            .receiverId(memberRepository.findById(transferHistoryDTO.getReceiverId())
		                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."))) // String을 Member로 변환
		            .senderId(memberRepository.findById(transferHistoryDTO.getSenderId())
		                    .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다."))) // String을 Member로 변환
		            .build();
		} else {
		    transferHistory = TransferHistory.builder()
		            .transferCode(reservationCode)
		            .payCode(payCode)
		            .amount(totalPrice)
		            .memo("시설예약")
		            .status("송금성공")
		            .transferDate(LocalDateTime.now())
		            .receiverId(memberRepository.findById(transferHistoryDTO.getReceiverId())
		                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."))) // String을 Member로 변환
		            .senderId(memberRepository.findById(transferHistoryDTO.getSenderId())
		                    .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다."))) // String을 Member로 변환
		            .clubCode(reservationDTO.getClubCode())
		            .build();
		}

		// Step 5: 데이터베이스에 저장
		reservationRepository.save(reservation);
		transferHistoryRepository.save(transferHistory);

		// Step 6: senderId(예약자)의 memMoney 업데이트
		Member sender = memberRepository.findById(transferHistoryDTO.getSenderId())
		        .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다."));
		BigDecimal updatedSenderMoney = sender.getMemMoney().subtract(totalPrice); // 기존 금액 - totalPrice
		sender.setMemMoney(updatedSenderMoney);
		memberRepository.save(sender);

		// Step 7: receiverId의 memMoney 업데이트
		Member receiver = memberRepository.findById(transferHistoryDTO.getReceiverId())
		        .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));
		BigDecimal updatedReceiverMoney = receiver.getMemMoney().add(totalPrice); // 기존 금액 + totalPrice
		receiver.setMemMoney(updatedReceiverMoney);
		memberRepository.save(receiver);

		// 로그 출력 (선택 사항)
		log.info("예약 정보: {}", reservation);
		log.info("송금 내역: {}", transferHistory);
		log.info("수신자 잔액 업데이트 완료: {}", updatedReceiverMoney);
	}

	@Override
	public List<ReservationDTO> getReservationsByFacilityCode(String facilityCode) {
	    // 1. facilityCode에 해당하는 Reservation 엔티티 목록 조회
	    List<Reservation> reservations = reservationRepository.findByFacilityCode(facilityCode);

	    // 2. ModelMapper를 사용해 변환
	    return reservations.stream()
	            .map(reservation -> modelMapper.map(reservation, ReservationDTO.class))
	            .collect(Collectors.toList());
	}


	@Override
	public List<Reservation> getReservationTimeList(String facilityCode, Date reservationDate) {
		
		List<Reservation> reservations = reservationRepository.findByFacilityCodeAndReservationDateAndDeleteFlagOrderByReservationStartTime(facilityCode, reservationDate, false);
		return reservations;
	}
	 @Override
	    public boolean isAlreadyCancelled(String reservationCode) {
	        return reservationRepository.existsByReservationCodeAndReservationProgress(reservationCode, "예약취소");
	    }
	 
	@Override
	@Transactional
	public void cancelBooking(String memId, TransferHistoryDTO transferHistoryDTO, ReservationDTO reservationDTO) {
	    // ✅ reservationDTO 검증
	    if (reservationDTO == null || reservationDTO.getReservationCode() == null) {
	        throw new IllegalArgumentException("reservationDTO 또는 reservationCode가 null입니다.");
	    }

	    // 1. 예약 정보 조회
	    Reservation reservation = reservationRepository.findById(reservationDTO.getReservationCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 예약 정보를 찾을 수 없습니다."));

	    // 2. TransferHistory 정보 조회
	    TransferHistory transferHistory = transferHistoryRepository.findByPayCode(reservation.getPayCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 이체 내역을 찾을 수 없습니다."));

	    // 3. Sender와 Receiver ID 확인
	    if (transferHistory.getSenderId().getMemId() == null || transferHistory.getReceiverId().getMemId() == null) {
	        throw new IllegalArgumentException("Sender ID 또는 Receiver ID가 null입니다.");
	    }

	    Member sender = memberRepository.findById(transferHistory.getSenderId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getSenderId()));

	    Member receiver = memberRepository.findById(transferHistory.getReceiverId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getReceiverId()));

	    // 금액 업데이트
	    BigDecimal updatedSenderMoney = sender.getMemMoney().add(transferHistory.getAmount()); // 기존 금액 - totalPrice
		sender.setMemMoney(updatedSenderMoney);
		
		BigDecimal updatedReceiverMoney = receiver.getMemMoney().subtract(transferHistory.getAmount()); // 기존 금액 + totalPrice
		receiver.setMemMoney(updatedReceiverMoney);
	    // 4. 새로운 TransferHistory 생성
	    TransferHistory newTransferHistory = TransferHistory.builder()
	            .transferCode(String.valueOf(System.currentTimeMillis()))
	            .payCode(UUID.randomUUID().toString())
	            .amount(transferHistory.getAmount())
	            .memo("시설예약 취소로 인한 환불")
	            .status("송금취소")
	            .transferDate(LocalDateTime.now())
	            .receiverId(receiver)
	            .senderId(sender)
	            .clubCode(reservation.getClubCode())
	            .build();

	    // 5. 예약 상태 업데이트
	    reservation.setMemo("예약자의 취소로 인한 예약 취소");
	    reservation.setReservationProgress("예약취소");
	    reservation.setDeleteFlag(true);

	    // ✅ 예약에 대한 일정 조회 후 삭제 플래그 업데이트
	    Optional<Member_Planner> optionalPlanner = memberPlannerRepository.findFirstByReservationCodeAndMemId(
	            reservationDTO.getReservationCode(), transferHistory.getSenderId().getMemId());

	    if (optionalPlanner.isPresent()) {
	        Member_Planner planner = optionalPlanner.get();
	        System.out.println("변경 전 deleteFlag: " + planner.getDeleteFlag());
	        planner.setDeleteFlag(true);
	        memberPlannerRepository.save(planner);
	        memberPlannerRepository.flush(); // ✅ 즉시 반영
	    } else {
	        throw new IllegalArgumentException("해당 예약 코드에 대한 Planner 데이터가 없습니다. 예약코드: " 
	            + reservationDTO.getReservationCode() + ", 회원ID: " + transferHistory.getSenderId().getMemId());
	    }

	    // 6. 데이터 저장
	    memberRepository.save(sender);
	    memberRepository.save(receiver);
	    transferHistoryRepository.save(newTransferHistory);
	    reservationRepository.save(reservation);
	}


	// 관리자가 승인거절 눌렀을때
	@Transactional
	@Override
	public void cancelBookingbyManager(String memId, TransferHistoryDTO transferHistoryDTO,
			ReservationDTO reservationDTO) {
		  // 1. 예약 정보 조회
	    Reservation reservation = reservationRepository.findById(reservationDTO.getReservationCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 예약 정보를 찾을 수 없습니다."));

	    // 2. TransferHistory 정보 조회
	    TransferHistory transferHistory = transferHistoryRepository.findByPayCode(reservation.getPayCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 이체 내역을 찾을 수 없습니다."));

	   
	    // 3. Sender와 Receiver ID 확인
	    if (transferHistory.getSenderId().getMemId() == null || transferHistory.getReceiverId().getMemId() == null) {
	        throw new IllegalArgumentException("Sender ID 또는 Receiver ID가 null입니다.");
	    }

	    Member sender = memberRepository.findById(transferHistory.getSenderId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getSenderId()));

	    Member receiver = memberRepository.findById(transferHistory.getReceiverId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getReceiverId()));

	    // 금액 업데이트
	    BigDecimal updatedSenderMoney = sender.getMemMoney().add(transferHistory.getAmount()); // 기존 금액 - totalPrice
		sender.setMemMoney(updatedSenderMoney);
		
		BigDecimal updatedReceiverMoney = receiver.getMemMoney().subtract(transferHistory.getAmount()); // 기존 금액 + totalPrice
		receiver.setMemMoney(updatedReceiverMoney);

	    // 4. 새로운 TransferHistory 생성
	    TransferHistory newTransferHistory = TransferHistory.builder()
	            .transferCode(String.valueOf(System.currentTimeMillis()))
	            .payCode(UUID.randomUUID().toString())
	            .amount(transferHistory.getAmount())
	            .memo("관리자의 승인거절로 인한 환불")
	            .status("송금취소")
	            .transferDate(LocalDateTime.now())
	            .receiverId(receiver)
	            .senderId(sender)
	            .clubCode(reservation.getClubCode())
	            .build();

	    // 5. 예약 상태 업데이트
	    reservation.setMemo("관리자의 승인 거절로 인한 예약취소");
	    reservation.setReservationProgress("예약취소");
	    reservation.setDeleteFlag(true);
	    
	    
	    Optional<Member_Planner> optionalPlanner = memberPlannerRepository.findFirstByReservationCodeAndMemId(reservationDTO.getReservationCode(), transferHistory.getSenderId().getMemId());

	    if (optionalPlanner.isPresent()) {
	        Member_Planner planner = optionalPlanner.get();
	        System.out.println("변경 전 deleteFlag: " + planner.getDeleteFlag());
	        planner.setDeleteFlag(true);
	        memberPlannerRepository.save(planner); // ✅ 단일 엔티티 저장
	    } else {
	        throw new IllegalArgumentException("해당 예약 코드에 대한 Planner 데이터가 없습니다. 예약코드: " 
	            + reservationDTO.getReservationCode() + ", 회원ID: " + transferHistory.getSenderId().getMemId());
	    }

	    
	    
	    // 6. 데이터 저장
	    memberRepository.save(sender);
	    memberRepository.save(receiver);
	    transferHistoryRepository.save(newTransferHistory);
	    reservationRepository.save(reservation);
		
	}
	
	// 시설삭제했을시, 해당 시설 코드의 예약들 전부 예약 취소 및 환불처리
	 @Transactional
	    public void cancelAllBookingByFacilityCode(String facilityCode) {
	        // 해당 시설과 관련된 모든 예약 조회
	        List<Reservation> reservations = reservationRepository.findByFacilityCode(facilityCode);

	        for (Reservation reservation : reservations) {
	            // 예약에 대한 이체 내역 조회
	            Optional<TransferHistory> transferHistoryOptional = transferHistoryRepository.findByPayCode(reservation.getPayCode());
	            
	            if (transferHistoryOptional.isPresent()) {
	                TransferHistory transferHistory = transferHistoryOptional.get();
	                
	                // Sender와 Receiver 정보 조회
	                Member sender = memberRepository.findById(transferHistory.getSenderId().getMemId())
	                        .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다."));
	                Member receiver = memberRepository.findById(transferHistory.getReceiverId().getMemId())
	                        .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));
	                
	                // 금액 업데이트 (환불 처리)
	        	    BigDecimal updatedSenderMoney = sender.getMemMoney().add(transferHistory.getAmount()); // 기존 금액 - totalPrice
	        		sender.setMemMoney(updatedSenderMoney);
	        		
	        		BigDecimal updatedReceiverMoney = receiver.getMemMoney().subtract(transferHistory.getAmount()); // 기존 금액 + totalPrice
	        		receiver.setMemMoney(updatedReceiverMoney);
	                
	                // 새로운 송금 취소 내역 추가
	                TransferHistory newTransferHistory = TransferHistory.builder()
	                        .transferCode(String.valueOf(System.currentTimeMillis()))
	                        .payCode(UUID.randomUUID().toString())
	                        .amount(transferHistory.getAmount())
	                        .memo("시설 삭제로 인한 환불")
	                        .status("송금취소")
	                        .transferDate(LocalDateTime.now())
	                        .receiverId(receiver)
	                        .senderId(sender)
	                        .clubCode(reservation.getClubCode())
	                        .build();
	                
	                // 예약 상태 업데이트
	                reservation.setMemo("시설삭제로 인한 예약취소");
	                reservation.setReservationProgress("예약취소");
	                reservation.setDeleteFlag(true);

	        	    
	                // 변경 사항 저장
	                memberRepository.save(sender);
	                memberRepository.save(receiver);
	                transferHistoryRepository.save(newTransferHistory);
	                reservationRepository.save(reservation);
	            }
	        }
	    }
	
	// 관리자가 승인거절눌렀다가 다시 승인 눌렀을때
	 @Transactional
	@Override
	public void cancelAndBookAgainbyManager(String memId, TransferHistoryDTO transferHistoryDTO,
			ReservationDTO reservationDTO) {
		  // 1. 예약 정보 조회
	    Reservation reservation = reservationRepository.findById(reservationDTO.getReservationCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 예약 정보를 찾을 수 없습니다."));

	    // 2. TransferHistory 정보 조회
	    TransferHistory transferHistory = transferHistoryRepository.findByPayCode(reservation.getPayCode())
	            .orElseThrow(() -> new IllegalArgumentException("해당 이체 내역을 찾을 수 없습니다."));

	   
	    
	    // 3. Sender와 Receiver ID 확인
	    if (transferHistory.getSenderId().getMemId() == null || transferHistory.getReceiverId().getMemId() == null) {
	        throw new IllegalArgumentException("Sender ID 또는 Receiver ID가 null입니다.");
	    }

	    Member sender = memberRepository.findById(transferHistory.getSenderId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getSenderId()));

	    Member receiver = memberRepository.findById(transferHistory.getReceiverId().getMemId())
	            .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다. ID: " + transferHistoryDTO.getReceiverId()));

	    // 금액 업데이트
	 // 금액 업데이트
	    BigDecimal updatedSenderMoney = sender.getMemMoney().subtract(transferHistory.getAmount()); // 기존 금액 - totalPrice
		sender.setMemMoney(updatedSenderMoney);
		
		BigDecimal updatedReceiverMoney = receiver.getMemMoney().add(transferHistory.getAmount()); // 기존 금액 + totalPrice
		receiver.setMemMoney(updatedReceiverMoney);

	    // 4. 새로운 TransferHistory 생성
	    TransferHistory newTransferHistory = TransferHistory.builder()
	            .transferCode(String.valueOf(System.currentTimeMillis()))
	            .payCode(UUID.randomUUID().toString())
	            .amount(transferHistory.getAmount())
	            .memo("관리자의 승인으로 송금")
	            .status("송금성공")
	            .transferDate(LocalDateTime.now())
	            .receiverId(receiver)
	            .senderId(sender)
	            .clubCode(reservation.getClubCode())
	            .build();

	    // 5. 예약 상태 업데이트
	    reservation.setMemo("관리자가 거절했다가 다시 승인으로 인한 예약완료");
	    reservation.setReservationProgress("예약완료");
	    reservation.setDeleteFlag(false);
	    
	    
	    Optional<Member_Planner> optionalPlanner = memberPlannerRepository.findFirstByReservationCodeAndMemId(reservationDTO.getReservationCode(), transferHistory.getSenderId().getMemId());

	    if (optionalPlanner.isPresent()) {
	        Member_Planner planner = optionalPlanner.get();
	        System.out.println("변경 전 deleteFlag: " + planner.getDeleteFlag());
	        planner.setDeleteFlag(false);
	        memberPlannerRepository.save(planner); // ✅ 단일 엔티티 저장
	    } else {
	        throw new IllegalArgumentException("해당 예약 코드에 대한 Planner 데이터가 없습니다. 예약코드: " 
	            + reservationDTO.getReservationCode() + ", 회원ID: " + transferHistory.getSenderId().getMemId());
	    }


	    
	    // 6. 데이터 저장
	    memberRepository.save(sender);
	    memberRepository.save(receiver);
	    transferHistoryRepository.save(newTransferHistory);
	    reservationRepository.save(reservation);
	}
	
	// 사설시설
	public List<FacilityDTO> getPrivateFacilityWithRadius(BigDecimal userLat, BigDecimal userLng, double radius) {
		List<Facility> facilities = facilityRepository.findPrivateFacilityWithRadius(userLat, userLng, radius);
		
		return facilities.stream()
				.map(facility -> modelMapper.map(facility, FacilityDTO.class))
				.collect(Collectors.toList());
	}
	
	// 공공시설
	@Override
	public List<FacilityDTO> getFacilityWithRadius(BigDecimal userLat, BigDecimal userLng, double radius) {
		List<Facility> facilities = facilityRepository.findFacilityWithRadius(userLat, userLng, radius);
		
		return facilities.stream()
					   .map(facility -> modelMapper.map(facility, FacilityDTO.class))
					   .collect(Collectors.toList());
	}
	
	// 공공 시설 검색
	@Override
	public List<Facility> getPublicFacility() {
		return facilityRepository.findPublic();
	}
	
	 /**
     * 주소 문자열에서 지역 정보 추출
     */
    public String extractRegionFromAddress(String facilityAddress) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
        String result = null;

        try {
            // URL 인코딩
            String query = URLEncoder.encode(facilityAddress, "UTF-8");
            String requestUrl = apiUrl + "?query=" + query;

            // HTTP 요청
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

            // 응답 처리
            int responseCode = connection.getResponseCode();
            System.out.println("Request URL: " + requestUrl);
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                System.out.println("Response: " + response.toString());

                // JSON 파싱
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray documents = jsonObject.getJSONArray("documents");

                if (documents.length() > 0) {
                    JSONObject address = documents.getJSONObject(0).getJSONObject("address");

                    // 시, 구, 동 정보 추출 및 조합
                    String region1 = address.optString("region_1depth_name", "시 정보 없음");
                    String region2 = address.optString("region_2depth_name", "구/군 정보 없음");
                    String region3 = address.optString("region_3depth_name", "동/면 정보 없음");

                    // 특별시, 광역시, 도 등의 이름 변환
                    if ("서울".equals(region1)) {
                        region1 = "서울특별시";
                    } else if ("부산".equals(region1)) {
                        region1 = "부산광역시";
                    } else if ("대구".equals(region1)) {
                        region1 = "대구광역시";
                    } else if ("인천".equals(region1)) {
                        region1 = "인천광역시";
                    } else if ("광주".equals(region1)) {
                        region1 = "광주광역시";
                    } else if ("대전".equals(region1)) {
                        region1 = "대전광역시";
                    } else if ("울산".equals(region1)) {
                        region1 = "울산광역시";
                    } else if ("세종".equals(region1)) {
                        region1 = "세종특별자치시";
                    }
                    
                    // 결과 조합
                    result = String.format("%s %s %s", region1, region2, region3);
                }
            } else {
                System.err.println("Error: HTTP response code " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result != null ? result : "지역 정보가 없습니다.";
    }

}