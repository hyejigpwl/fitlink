package com.lec.packages.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.ChargeHistory;
import com.lec.packages.domain.Member;
import com.lec.packages.domain.MemberRole;
import com.lec.packages.domain.TransferHistory;
import com.lec.packages.domain.exercise_code_table;
import com.lec.packages.dto.ChargeHistoryDTO;
import com.lec.packages.dto.MemberJoinDTO;
import com.lec.packages.dto.TransferHistoryDTO;
import com.lec.packages.repository.ChargeHistoryRepository;
import com.lec.packages.repository.ExerciseRepository;
import com.lec.packages.repository.MemberRepository;
import com.lec.packages.repository.TransferHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final ModelMapper modelMapper;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ExerciseRepository exerciseRepository;
	private final ChargeHistoryRepository chargeHistoryRepository;
	private final EmailService emailService; 
//	private final Member member;

	@Override
	public void join(MemberJoinDTO memberJoinDTO, String storedFileName) /* throws MidExistException */ {
//		String mid = memberJoinDTO.getMemId();
//		boolean exist = memberRepository.existsById(mid);
//		if(exist) {
//			throw new MidExistException("동일한 아이디가 존재합니다.");
//		}

		Member member = modelMapper.map(memberJoinDTO, Member.class);
		member.changePassword(passwordEncoder.encode(memberJoinDTO.getMemPw()));

		// MEM_ISMANAGER 값이 true(1)인 경우 ROLE_ADMIN 추가
		if (memberJoinDTO.isMemIsmanager()) {
			member.addRole(MemberRole.ADMIN);
		} else {
			member.addRole(MemberRole.USER);
		}

		member.setMemPicture(storedFileName);

		// log.info("========>" + member + "[" + member.getRoleSet() + "]");

		memberRepository.save(member);
	}

	@Override
	public boolean isDuplicateId(String memId) {
		return memberRepository.existsById(memId); // 존재하면 true 반환
	}

	@Override
	public void modify(MemberJoinDTO memberJoinDTO, String storedFileName) {
		// 1. 기존 회원 정보 가져오기
		Optional<Member> existingMember = memberRepository.findById(memberJoinDTO.getMemId());

		if (existingMember.isPresent()) {
			Member member = existingMember.get(); // 기존 회원 정보 가져오기

			// 기존 memSocial 값 유지
			boolean existingMemSocial = member.isMemSocial();
			
			// 기존 비밀번호 값
			String existingMemPw = member.getMemPw();

			// `memExercise` 영속 상태 설정
			if (memberJoinDTO.getMemExercise() != null) {
				exercise_code_table exercise = exerciseRepository
						.findById(memberJoinDTO.getMemExercise().getEXERCISE_CODE())
						.orElseThrow(() -> new IllegalArgumentException(
								"Exercise not found with code: " + memberJoinDTO.getMemExercise().getEXERCISE_CODE()));
				member.setMemExercise(exercise);
			} else {
				member.setMemExercise(null); // 선택되지 않은 경우 null로 설정
			}

			// `memClub` 영속 상태 설정
			if (memberJoinDTO.getMemClub() != null) {
				exercise_code_table club = exerciseRepository.findById(memberJoinDTO.getMemClub().getEXERCISE_CODE())
						.orElseThrow(() -> new IllegalArgumentException(
								"Club not found with code: " + memberJoinDTO.getMemClub().getEXERCISE_CODE()));
				member.setMemClub(club);
			} else {
				member.setMemClub(null); // 선택되지 않은 경우 null로 설정
			}

			// 기타 정보 업데이트
			member.setMemName(memberJoinDTO.getMemName());
			member.setMemNickname(memberJoinDTO.getMemNickname());
			member.setMemPicture(storedFileName);
			member.setMemIntroduction(memberJoinDTO.getMemIntroduction());
			member.setMemTell(memberJoinDTO.getMemTell());
			member.setMemBirthday(memberJoinDTO.getMemBirthday());
			member.setMemEmail(memberJoinDTO.getMemEmail());
			member.setMemAddress(memberJoinDTO.getMemAddress());
			member.setMemAddressDetail(memberJoinDTO.getMemAddressDetail());
			member.setMemZipcode(memberJoinDTO.getMemZipcode());

			// 기존 memSocial 값 유지
			member.setMemSocial(existingMemSocial);
			
			// ✅ 기존 비밀번호 유지 로직 추가
			// ✅ 비밀번호 변경 여부 확인
	        if (memberJoinDTO.getMemPw() == null || memberJoinDTO.getMemPw().trim().isEmpty()) {
	            // 사용자가 비밀번호를 변경하지 않은 경우 기존 비밀번호 유지
	            member.setMemPw(existingMemPw);
	        } else {
	            // 사용자가 새로운 비밀번호를 입력한 경우 암호화하여 저장
	            member.setMemPw(passwordEncoder.encode(memberJoinDTO.getMemPw()));
	        }

			// 저장
			memberRepository.save(member);
		} else {
			throw new IllegalArgumentException("Member not found with ID: " + memberJoinDTO.getMemId());
		}
	}

	@Override
	public void remove(String username) {
		// 사용자 조회
		Member member = memberRepository.findById(username)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		// DELETE_FLAG를 true(1)로 설정
		member.setDeleteFlag(true);

		// 변경된 내용을 저장
		memberRepository.save(member);
	}

	@Override
	public void updateMemAddressSet(String memberId, String memAddressSet) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		member.setMemAddressSet(memAddressSet);
		memberRepository.save(member);
	}

	// 금액 충전
	@Override
	public void chargePoint(String id, BigDecimal amount, BigDecimal plusPoint) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		member.setMemMoney(amount);
		memberRepository.save(member);

		// 충전 내역 저장
		ChargeHistory chargeHistory = ChargeHistory.builder().chargeCode(generateChargeCode()) // 고유 코드 생성 메서드
				.memId(id).amount(plusPoint).chargeDate(LocalDateTime.now()).paymentMethod("CARD") // 결제 방법 (예: 카드 결제)
				.status("충전성공") // 상태
				.build();

		chargeHistoryRepository.save(chargeHistory);
	}

	// 고유 코드 생성 (UUID 사용 예제)
	private String generateChargeCode() {
		return "" + System.currentTimeMillis();
	}

	@Autowired
	private TransferHistoryRepository transferHistoryRepository;

	public List<TransferHistoryDTO> getTransferHistories(String memId) {
		List<TransferHistory> transferHistories = transferHistoryRepository.findByMemId(memId);
		return transferHistories.stream()
				.map(th -> TransferHistoryDTO.builder().transferCode(th.getTransferCode())
						.senderId(th.getSenderId().getMemId()).receiverId(th.getReceiverId().getMemId())
						.amount(th.getAmount()).transferDate(th.getTransferDate()).status(th.getStatus())
						.memo(th.getMemo()).clubCode(th.getClubCode()).build())
				.collect(Collectors.toList());
	}

	// 관리자 계정에서 송금내역 불러오기
	public List<TransferHistoryDTO> getTransferHistorieWhenManager(String memId) {
		List<TransferHistory> transferHistoriesWhenManager = transferHistoryRepository.findByReceiverMemId(memId);
		return transferHistoriesWhenManager.stream()
				.map(th -> TransferHistoryDTO.builder().transferCode(th.getTransferCode())
						.senderId(th.getSenderId().getMemId()).receiverId(th.getReceiverId().getMemId())
						.amount(th.getAmount()).transferDate(th.getTransferDate()).status(th.getStatus())
						.memo(th.getMemo()).clubCode(th.getClubCode()).build())
				.collect(Collectors.toList());
	}

	public List<ChargeHistoryDTO> getChargeHistories(String memId) {
		List<ChargeHistory> chargeHistories = chargeHistoryRepository.findByMemId(memId);
		return chargeHistories.stream()
				.map(ch -> ChargeHistoryDTO.builder().chargeCode(ch.getChargeCode()).memId(ch.getMemId())
						.amount(ch.getAmount()).chargeDate(ch.getChargeDate()).paymentMethod(ch.getPaymentMethod())
						.status(ch.getStatus()).build())
				.collect(Collectors.toList());

	}

//	public void saveMemberFile(MemberSecurityDTO memberSecurityDTO, String fileName) {
//		// 업로드된 파일명을 MemberJoinDTO에 설정
//		memberSecurityDTO.setMEM_PICTURE(fileName);
//
//		// DTO를 엔티티로 변환 후 DB에 저장 (변환 작업은 필요시 추가)
//		memberRepository.save(member); // `toEntity`는 필요시 구현
//	}

	// 비밀번호 찾기
	@Transactional
    public boolean processFindPassword(String memId, String memName) {
        Optional<Member> optionalMember = memberRepository.findByMemIdAndMemName(memId, memName);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            
            // 1. 임시 비밀번호 생성
            String tempPassword = generateTempPassword();

            // 2. 비밀번호 암호화 후 저장
            member.setMemPw(passwordEncoder.encode(tempPassword));
            memberRepository.save(member);

            // 3. 이메일 전송
            emailService.sendEmail(member.getMemEmail(), "FITLINK 임시 비밀번호 안내", tempPassword);


            return true;
        }

        return false;
    }

    // 8자리 랜덤 비밀번호 생성
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}