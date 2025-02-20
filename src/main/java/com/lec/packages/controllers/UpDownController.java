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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lec.packages.dto.UploadFileDTO;
import com.lec.packages.dto.UploadResultDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;

@Log4j2
@RestController
public class UpDownController {

	@Value("${com.lec.upload.path}")
	private String uploadPath;


	 @Operation(summary = "File Upload Post", description = "POST 방식으로 파일 업로드!!")
	 @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	 public List<UploadResultDTO> upload(@RequestBody UploadFileDTO uploadFileDTO, HttpServletRequest request) {
	     log.info(uploadFileDTO);

	     if (uploadFileDTO.getFiles() != null) {
	         final List<UploadResultDTO> list = new ArrayList<>();

	         uploadFileDTO.getFiles().forEach(multipartFile -> {
	             String originalFileName = multipartFile.getOriginalFilename();
	             String uuid = UUID.randomUUID().toString();

	             Path savePath = Paths.get(uploadPath, uuid + "_" + originalFileName);
	             boolean isImage = false;

	             try {
	                 multipartFile.transferTo(savePath); // 실제 파일 저장

	                 // 이미지 파일 처리
	                 if (Files.probeContentType(savePath).startsWith("image")) {
	                     isImage = true;
	                     File thumbFile = new File(uploadPath, "s_" + uuid + "_" + originalFileName);
	                     Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
	                 }

	                 // 저장된 파일명 생성
	                 String storedFileName = isImage ? "s_" + uuid + "_" + originalFileName : uuid + "_" + originalFileName;

	              // 세션에 저장된 파일 이름 설정
	                 HttpSession session = request.getSession();
	                 session.setAttribute("storedFileName", storedFileName);

	                 // 업로드 결과 추가
	                 list.add(UploadResultDTO.builder()
	                         .uuid(uuid)
	                         .fileName(originalFileName)
	                         .img(isImage)
	                         .build());
	             } catch (IOException e) {
	                 log.error("파일 저장 중 오류 발생", e);
	             }
	         });

	         return list;
	     }

	     return null;
	 }

	 @Operation(summary = "파일보기", description = "GET방식으로 첨부파일조회!!")
		@GetMapping(value = "/view/{fileName}")
		public ResponseEntity<Resource> viewFileGET(@RequestBody @PathVariable("fileName") String fileName) {
			
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

	
}