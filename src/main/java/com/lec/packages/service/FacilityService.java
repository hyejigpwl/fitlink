package com.lec.packages.service;


import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.lec.packages.domain.Facility;
import com.lec.packages.domain.Reservation;
import com.lec.packages.dto.FacilityDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.ReservationDTO;
import com.lec.packages.dto.TransferHistoryDTO;


public interface FacilityService {

	String register(FacilityDTO facilityDTO);
	PageResponseDTO<FacilityDTO> listByUser(String memId, PageRequestDTO pageRequestDTO);

	FacilityDTO getFacilityByCode(String facilityCode);
	void modify(FacilityDTO facilityDTO);
	void remove(String facilityCode);
	void bookByMember(TransferHistoryDTO transferHistoryDTO, ReservationDTO reservationDTO,BigDecimal memMoney);
	List<ReservationDTO> getReservationsByFacilityCode(String facilityCode); 
	
	PageResponseDTO<FacilityDTO> listAllFacility(PageRequestDTO pageRequestDTO
			,String facilityAddress, String exerciseCode, Boolean facilityIsOnlyClub);

	PageResponseDTO<FacilityDTO> listPrivateFacility(PageRequestDTO pageRequestDTO, String facilityAddress,
			String exerciseCode, Boolean facilityIsOnlyClub);
	
	PageResponseDTO<FacilityDTO> listPublicFacility(PageRequestDTO pageRequestDTO, String facilityAddress,
			String exerciseCode, Boolean facilityIsOnlyClub);
	
    List<Reservation> getReservationTimeList(String facilityCode, Date reservationDate);
	void cancelBooking(String memId, TransferHistoryDTO transferHistoryDTO, ReservationDTO reservationDTO);
	void cancelBookingbyManager(String memId, TransferHistoryDTO transferHistoryDTO, ReservationDTO reservationDTO);

	List<FacilityDTO> getPrivateFacilityWithRadius(BigDecimal lat, BigDecimal longt, double radius);
	List<FacilityDTO> getFacilityWithRadius(BigDecimal userLat, BigDecimal userLng, double radius);

	List<Facility> getPublicFacility();
	void cancelAndBookAgainbyManager(String memId, TransferHistoryDTO transferHistoryDTO,
			ReservationDTO reservationDTO);
	void cancelAllBookingByFacilityCode(String facilityCode);
	boolean isAlreadyCancelled(String reservationCode);
	
	
	

}
