package com.lec.packages.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
public abstract class BaseEntity {
    
    // 생성 일자
    // updatable = false => 데이터 생성할 때만 삽입
    @CreatedDate
    @Column(name = "CREATEDATE", updatable = false)
    private LocalDateTime CREATEDATE;

    // 마지막 수정 일자
    @LastModifiedDate
    @Column(name = "MODIFYDATE")
    private LocalDateTime MODIFYDATE;
}
