package com.lec.packages.domain;

import com.lec.packages.domain.primaryKeyClasses.ClubMemberKeyClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ClubMemberKeyClass.class)
@ToString
public class Club_Member_List extends BaseEntity {

	@Id
    @JoinColumn(name = "MEM_ID")
    @Column(nullable = false, length = 100, name = "MEM_ID")
    private String memId;
    
	@Id
    @JoinColumn(name = "CLUB_CODE")
    @Column(length = 10, name = "CLUB_CODE")
    private String clubCode;
    
    @Column(name = "REPORT_COUNT")
    private int reportCount;
    
    @Column(name = "DELETE_FLAG")
    private boolean deleteFlag;    
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "MEM_ID", insertable = false, updatable = false)
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "CLUB_CODE", insertable = false, updatable = false)
    private Club club;
    
}
