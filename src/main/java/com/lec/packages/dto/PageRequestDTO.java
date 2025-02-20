package com.lec.packages.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 12;

    // 검색 유형
    @Builder.Default
    private String type = "a";
    
    //검색키워드를 배열로 수정 
    private String[] keywords;

    public String[] getTypes() {
        if(type == null || type.isEmpty()) return null;

        return type.split("/");
    }

    //Pageable 객체 생성 수정된사항 
    public Pageable getPageable(String...props) {
    	return PageRequest.of(
                Math.max(this.page - 1, 0), // 페이지 번호는 0부터 시작 ★ 수정된 부분: 음수 방지
                Math.max(this.size, 1), // 최소 크기는 1 ★ 수정된 부분: 음수 방지
                Sort.by(props).descending() // 정렬 기준
            );
    }

    //키워드 배열 지원 (수정된사항)
    private String link;

    public String getLink() {
    	 if (link == null) {
             StringBuilder builder = new StringBuilder();

             builder.append("page=").append(this.page);
             builder.append("&size=").append(this.size);

             if (type != null && !type.isEmpty()) {
                 builder.append("&type=").append(type);
             }

             if (keywords != null && keywords.length > 0) { // 키워드 배열 처리 추가 ★ 추가된 부분
                 try {
                     String encodedKeywords = Stream.of(keywords)
                         .map(keyword -> {
                             try {
                                 return URLEncoder.encode(keyword, "utf-8");
                             } catch (UnsupportedEncodingException e) {
                                 return ""; // 인코딩 실패 시 빈 문자열 반환 ★ 추가된 부분
                             }
                         })
                         .collect(Collectors.joining(",")); // 키워드를 ','로 연결 ★ 추가된 부분
                     builder.append("&keywords=").append(encodedKeywords);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }

             link = builder.toString();
         }

         return link;
     }

}
