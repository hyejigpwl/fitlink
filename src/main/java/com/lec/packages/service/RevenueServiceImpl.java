package com.lec.packages.service;



import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lec.packages.domain.TransferHistory;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;
import com.lec.packages.dto.SalesDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.TransferHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class RevenueServiceImpl implements RevenueService{
	
	private final TransferHistoryRepository transferHistoryRepository;
	private final ModelMapper modelMapper;
	
	@Override
	public List<SalesDTO> getSalesData(String memId) {
        return transferHistoryRepository.findSalesDetailsByMemId(memId);
	}
	
	

}
