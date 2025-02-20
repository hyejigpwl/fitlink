package com.lec.packages.service;


import java.util.List;
import java.util.Optional;

import com.lec.packages.domain.Reservation;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.ReservationDTO;

public interface ReservationService {

	// PageResponseDTO<ReservationDTO> getReservationByFacilityCode(String facilityCode, PageRequestDTO pageRequestDTO);

	PageResponseDTO<ReservationDTO> getAllReservationsForUser(String memId, PageRequestDTO pageRequestDTO);
	List<ReservationDTO> getAllReservationsForUser(String memId);
	PageResponseDTO<ReservationDTO> getConfirmReservationsForUser(String memId, PageRequestDTO pageRequestDTO);
	PageResponseDTO<ReservationDTO> getRefuseReservationsForUser(String memId, PageRequestDTO pageRequestDTO);
	PageResponseDTO<ReservationDTO> getInprogressReservationsForUser(String memId, PageRequestDTO pageRequestDTO);

	ReservationDTO getReservationByCode(String reservationCode);

	List<ReservationDTO> getConfirmedReservationsForUser(String memId);

	Optional<Reservation> getFacilityNameByCode(String reservationCode);

//	MemberSecurityDTO getMemberInfoByReservationCode(String reservationCode);

	void cancelExpiredReservation();
	ReservationDTO getReservationBypayCodeAndDeleteFlag(String payCode);
	
	
}
