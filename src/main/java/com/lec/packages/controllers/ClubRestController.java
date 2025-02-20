package com.lec.packages.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lec.packages.domain.Club;
import com.lec.packages.dto.ClubBoardAllListDTO;
import com.lec.packages.dto.ClubBoardDTO;
import com.lec.packages.dto.ClubBoardReplyDTO;

import com.lec.packages.dto.ClubDTO;
import com.lec.packages.dto.ClubReservationDTO;
import com.lec.packages.dto.PageRequestDTO;
import com.lec.packages.dto.PageResponseDTO;

import com.lec.packages.dto.UploadFileDTO;
import com.lec.packages.dto.UploadResultDTO;
import com.lec.packages.repository.ClubRepository;
import com.lec.packages.repository.ClubReservationMemberRepository;
import com.lec.packages.service.ClubService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;

@Log4j2
@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubRestController {
    
    @Value("${com.lec.upload.path}")
    private String uploadPath;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private ClubRepository clubRepository;  
    
    @Autowired
    private ClubReservationMemberRepository clubReservationMemberRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> uploadFile(@ModelAttribute UploadFileDTO uploadFileDTO){
        log.info("do upLoadFileController");
        
        if(uploadFileDTO.getFiles() != null){
            List<UploadResultDTO> list;

            list = new ArrayList<>();
            uploadFileDTO.getFiles().forEach(multipartFile -> {
                String originalFileName = multipartFile.getOriginalFilename();
				log.info(originalFileName);
				String uuid = UUID.randomUUID().toString();
				log.info(uuid);
				
				Path savePath = Paths.get(uploadPath, uuid + "_" + originalFileName);
				boolean isImage = false;
				
				try {
					multipartFile.transferTo(savePath); // 실제로 저장할 파일 위치
					
					// 이미지파일이라면
					if(Files.probeContentType(savePath).startsWith("image")) {
						isImage = true;
						File thumbFile = new File(uploadPath, "s_" + uuid + "_" + originalFileName);
						Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}  
				
				list.add(UploadResultDTO.builder()
						.uuid(uuid)
						.fileName(originalFileName)
						.img(isImage)
						.build());
            });
            return list;
        }
        return null;
    }
    
	@GetMapping(value = "/view/{fileName}")
	public ResponseEntity<Resource> viewImgFile(@RequestBody @PathVariable("fileName") String fileName) {

		Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

		HttpHeaders headers = new HttpHeaders();
		
		try {
			headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}
		
		return ResponseEntity.ok().headers(headers).body(resource);
	}


	@Operation(summary = "파일삭제", description = "DELETE 방식으로 첨부파일 삭제")
	@DeleteMapping(value = "/remove/{fileName}")
	public ResponseEntity<Map<String, Boolean>> removeFile(@PathVariable("fileName") String fileName) {
	    Map<String, Boolean> resultMap = new HashMap<>();
	    boolean removed = false;
	    try {
	        // 전달된 파일 이름이 썸네일 파일(s_)이라면, 원본 파일 이름 추출
	        String originalFileName = fileName.startsWith("s_") ? fileName.substring(2) : fileName;
	        // 원본 파일 경로
	        File originalFile = new File(uploadPath + File.separator + originalFileName);
	        // 썸네일 파일 경로
	        File thumbnailFile = new File(uploadPath + File.separator + fileName);
	        // 원본 파일 삭제
	        if (originalFile.exists()) {
	            String contentType = Files.probeContentType(originalFile.toPath());
	            removed = originalFile.delete();
	            // 썸네일 파일 삭제 (이미지일 경우만)
	            if (contentType != null && contentType.startsWith("image") && thumbnailFile.exists()) {
	                thumbnailFile.delete();
	            }
	        } else {
	            log.warn("파일이 존재하지 않습니다: {}", originalFileName);
	        }	       
	        
	    } catch (IOException e) {
	        log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("result", false));
	    }
	    resultMap.put("result", removed);
	    return ResponseEntity.ok(resultMap);
	}

	@PostMapping(value = "/replies/register/", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Integer> register(@RequestBody ClubBoardReplyDTO clubBoardReplyDTO, BindingResult bindingResult) throws BindException{
		
		log.info("do replyRegister");
		log.info(clubBoardReplyDTO);

		if(bindingResult.hasErrors()){
			throw new BindException(bindingResult);
		}

		Map<String, Integer> resultMap = new HashMap<>();
		int replyNo = clubService.registerReply(clubBoardReplyDTO);
		resultMap.put("replyNo", replyNo);
		
		return resultMap;
	}
	
	@GetMapping(value = "/replies/list/{boardNo},{clubCode}")
	public PageResponseDTO<ClubBoardReplyDTO> getList(@PathVariable("boardNo") int boardNo, @PathVariable("clubCode") String clubCode, PageRequestDTO pageRequestDTO) {

		log.info("do ReplyList");
		log.info(pageRequestDTO);
		PageResponseDTO<ClubBoardReplyDTO> responseDTO = clubService.getReplyListOfBoard(boardNo, clubCode, pageRequestDTO);
		return responseDTO;
		
	}

	// 클럽생성
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> createClub(
	        @RequestPart(value = "files", required = false) List<MultipartFile> files,
	        @RequestParam("clubName") String clubName,
	        @RequestParam("memId") String memId,
	        @RequestParam("clubExercise") String clubExercise,
	        @RequestParam("clubTheme") String clubTheme,
	        @RequestParam("clubIntroduction") String clubIntroduction,
	        @RequestParam("clubAddress") String clubAddress,
	        @RequestParam(value = "clubIsprivate", required = false) Boolean clubIsprivate,
	        @RequestParam(value = "clubPw", required = false) String clubPw) {

	    Map<String, Object> response = new HashMap<>();
	    List<String> imagePaths = new ArrayList<>();

	    try {
	        if (files != null && !files.isEmpty()) {
	            for (MultipartFile file : files) {
	                String originalFileName = file.getOriginalFilename();
	                if (originalFileName == null || originalFileName.isEmpty()) {
	                    continue;
	                }

	                String uuid = UUID.randomUUID().toString();
	                String fileName = uuid + "_" + originalFileName;
	                Path filePath = Paths.get(uploadPath, fileName);

	                file.transferTo(filePath);

	                String contentType = Files.probeContentType(filePath);
	                if (contentType != null && contentType.startsWith("image")) {
	                    File thumbnail = new File(uploadPath, "s_" + fileName);
	                    Thumbnailator.createThumbnail(filePath.toFile(), thumbnail, 200, 200);
	                }

	                imagePaths.add(fileName);
	            }
	        }

	        ClubDTO clubDTO = ClubDTO.builder()
	                .clubName(clubName)
	                .memId(memId)
	                .clubExercise(clubExercise)
	                .clubTheme(clubTheme)
	                .clubIntroduction(clubIntroduction)
	                .clubAddress(clubAddress)
	                .clubIsprivate(clubIsprivate != null && clubIsprivate)
	                .clubPw(clubIsprivate != null && clubIsprivate ? clubPw : null)
	                .clubImage1(imagePaths.size() > 0 ? imagePaths.get(0) : null)
	                .clubImage2(imagePaths.size() > 1 ? imagePaths.get(1) : null)
	                .clubImage3(imagePaths.size() > 2 ? imagePaths.get(2) : null)
	                .clubImage4(imagePaths.size() > 3 ? imagePaths.get(3) : null)
	                .build();

	        String clubCode = clubService.create(clubDTO);

	        clubService.join(memId, clubCode, clubPw);

	        response.put("success", true);
	        response.put("redirectUrl", "/");
	        return ResponseEntity.status(HttpStatus.FOUND)
	                .header(HttpHeaders.LOCATION, "/")
	                .body(response);
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "클럽 생성 중 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	// 클럽수정
	@PostMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> clubModify(
	        @ModelAttribute ClubDTO clubDTO,
	        @RequestPart(value = "files", required = false) List<MultipartFile> files,
	        HttpServletRequest request,
	        Model model,
	        PageRequestDTO pageRequestDTO,
	        RedirectAttributes redirectAttributes) {

	    String requestURI = request.getRequestURI();
	    model.addAttribute("currentURI", requestURI);

	    try {
	        Optional<Club> optionalClub = clubRepository.findById(clubDTO.getClubCode());
	        Club club = optionalClub.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클럽입니다."));        
	        
	        if (files != null && !files.isEmpty()) {
	            for (int i = 0; i < files.size(); i++) {
	                MultipartFile file = files.get(i);
	                if (file != null && !file.isEmpty()) {
	                    String originalFileName = file.getOriginalFilename();
	                    String uuid = UUID.randomUUID().toString();
	                    String fileName = uuid + "_" + originalFileName;

	                    Path filePath = Paths.get(uploadPath, fileName);
	                    file.transferTo(filePath);

	                    String contentType = Files.probeContentType(filePath);
	                    if (contentType != null && contentType.startsWith("image")) {
	                        File thumbnail = new File(uploadPath, "s_" + fileName);
	                        Thumbnailator.createThumbnail(filePath.toFile(), thumbnail, 200, 200);
	                    }

	                    switch (i) {
	                        case 0 -> clubDTO.setClubImage1(fileName);
	                        case 1 -> clubDTO.setClubImage2(fileName);
	                        case 2 -> clubDTO.setClubImage3(fileName);
	                        case 3 -> clubDTO.setClubImage4(fileName);
	                    }
	                }
	            }
	        }

	        if (clubDTO.getClubImage1() == null) clubDTO.setClubImage1(club.getClubImage1());
	        if (clubDTO.getClubImage2() == null) clubDTO.setClubImage2(club.getClubImage2());
	        if (clubDTO.getClubImage3() == null) clubDTO.setClubImage3(club.getClubImage3());
	        if (clubDTO.getClubImage4() == null) clubDTO.setClubImage4(club.getClubImage4());

	        clubService.modify(clubDTO);
	        log.info(clubDTO.getClubAddress());

	        String redirectUrl = String.format("./club_detail?clubCode=%s", clubDTO.getClubCode());
	        return ResponseEntity.status(HttpStatus.FOUND)
	                .header(HttpHeaders.LOCATION, redirectUrl)
	                .build();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
	    }
	}
	
	@DeleteMapping("/removeImage")
	public ResponseEntity<Map<String, Boolean>> removeImage(@RequestParam("fileName")String fileName,
										@RequestParam("imageField") String imageField,
										@RequestParam("clubCode") String clubCode) {
		Map<String, Boolean> resultMap = new HashMap<>();
		boolean removed = false;
		
		try {
			Optional<Club> optional = clubRepository.findById(clubCode);
			Club club = optional.orElseThrow(() -> new IllegalArgumentException("클럽이 존재하지 않습니다"));
	        
			String originalFileName = fileName.startsWith("s_") ? fileName.substring(2) : fileName;
	        File originalFile = new File(uploadPath + File.separator + originalFileName);
	        File thumbnailFile = new File(uploadPath + File.separator + fileName);
	        
	        // 원본 파일 삭제
	        if (originalFile.exists()) {
	            String contentType = Files.probeContentType(originalFile.toPath());
	            removed = originalFile.delete();
	            // 썸네일 파일 삭제 (이미지일 경우만)
	            if (contentType != null && contentType.startsWith("image") && thumbnailFile.exists()) {
	                thumbnailFile.delete();
	            }
	        } else {
	            log.warn("파일이 존재하지 않습니다: {}", originalFileName);
	        }	       
	        
	        if(removed) {
	        	switch (imageField) {
	        		case "clubImage1" :
	        			club.setClubImage1(null);
	        			break;
	        		case "clubImage2" :
	        			club.setClubImage2(null);
	        			break;
	        		case "clubImage3" :
	        			club.setClubImage3(null);
	        			break;
	        		case "clubImage4" :
	        			club.setClubImage4(null);
	        			break;
	        	}
	        	clubRepository.save(club);
	        }	        
		    resultMap.put("result", removed);
		    return ResponseEntity.ok(resultMap);
		} 
	    catch (IOException e) {
	        log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("result", false));
	    }
	}
	
	
	@GetMapping("club/replies/getReply/{clubCode},{boardNo},{replyNo}")
	public ClubBoardReplyDTO getReplyDTO(@PathVariable("clubCode") String clubCode, @PathVariable("boardNo") int boardNo, @PathVariable("replyNo") int replyNo) {
		ClubBoardReplyDTO replyDTO = clubService.readReply(clubCode, boardNo, replyNo);
		return replyDTO;
	}

	@PostMapping(value = "/replies/modify/", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Integer> modifyReply(@RequestBody ClubBoardReplyDTO replyDTO) {

		log.info("do modifyReply");
		log.info(replyDTO);
		clubService.modifyReply(replyDTO);
		
		Map<String, Integer> resultMap = new HashMap<>();
		resultMap.put("replyNo", replyDTO.getReplyNo());

		return resultMap;
	}

	@DeleteMapping("replies/delete/{clubCode},{boardNo},{replyNo}")
	public Map<String, Integer> deleteReply(@PathVariable("clubCode") String clubCode, @PathVariable("boardNo") int boardNo, @PathVariable("replyNo") int replyNo){
		log.info("do deleteReply");

		int resultReplyNo = clubService.deleteReply(clubCode, boardNo, replyNo);

		Map<String, Integer> resultMap = new HashMap<>();
		resultMap.put("replyNo", resultReplyNo);

		return resultMap;
	}

	@GetMapping("/club_board_rest")
	public PageResponseDTO<ClubBoardAllListDTO> clubBoard(@RequestParam("clubCode") String clubCode, PageRequestDTO pageRequestDTO
			, @RequestParam("page") int page, @RequestParam("size") int size
			, HttpServletRequest request, Model model) {
		String requestURI = request.getRequestURI();
        model.addAttribute("currentURI", requestURI);
        
		log.info("do clubBoardListRest");
		pageRequestDTO.setSize(size);
		pageRequestDTO.setPage(page);
		pageRequestDTO.setType("ALL");
		PageResponseDTO<ClubBoardAllListDTO> boardDTO = clubService.listWithAll(pageRequestDTO, clubCode);
        log.info(boardDTO);

		return boardDTO;
	}

	@GetMapping("/myClubList")
	public List<ClubDTO> getMyClubList(HttpServletRequest request, Model model, @AuthenticationPrincipal User user) {
		String requestURI = request.getRequestURI();
		log.info("do myClubList");
        model.addAttribute("currentURI", requestURI);
		
		List<ClubDTO> myClubList = clubService.clubListWithMemID(user.getUsername());

		return myClubList;
	}

	@GetMapping("/ownerClubList")
	public List<ClubDTO> getOwnerClubList(HttpServletRequest request, Model model, @AuthenticationPrincipal User user){
		String requestURI = request.getRequestURI();
		log.info("do getOwnerClubList");
		model.addAttribute("currentURI", requestURI);

		List<ClubDTO> ownerClubList = clubService.ownerClubListWithMemId(user.getUsername());

		return ownerClubList;
	}

	@GetMapping("/getClubResList/{clubCode}")
	public List<ClubReservationDTO> getClubResList(HttpServletRequest request, Model model, @PathVariable("clubCode") String clubCode) {
		String requestURI = request.getRequestURI();
		log.info("do getClubResList");
		model.addAttribute("currentURI", requestURI);

		List<ClubReservationDTO> clubReservationDTOs = clubService.getClubResList(clubCode);		

		return clubReservationDTOs;
	}

	@GetMapping("/addClubResMember/{reservationCode},{clubCode}")
	public String addClubResMember(HttpServletRequest request, Model model, @PathVariable("reservationCode") String reservationCode, @PathVariable("clubCode") String clubCode
									, @AuthenticationPrincipal User user) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);
	    
	    log.info("do addClubResMember");
	    log.info(reservationCode);
	    log.info(clubCode);
	    
	 	String addResult = clubService.addClubResMember(reservationCode, clubCode, user.getUsername());							
		return addResult;
	}

	@GetMapping("/getBoardListByMemID")
	public List<ClubBoardDTO> getBoardListByMemID(HttpServletRequest request, Model model, @AuthenticationPrincipal User user) {
		String requestURI = request.getRequestURI();
		model.addAttribute("currentURI", requestURI);

		log.info("do getBoardListByMemID");

		List<ClubBoardDTO> dtos = clubService.getBoardListByMemID(user.getUsername());

		return dtos;
	}
	
	@GetMapping("/checkJoinStatus/{clubCode}")
	public ResponseEntity<Boolean> checkJoinStatus(@PathVariable("clubCode") String clubCode, 
	                                               @AuthenticationPrincipal User user) {
	    boolean isJoin = clubService.isJoinMember(user.getUsername(), clubCode);
	    return ResponseEntity.ok(isJoin);
	}
}
