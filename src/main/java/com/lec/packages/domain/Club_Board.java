package com.lec.packages.domain;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import com.lec.packages.domain.primaryKeyClasses.ClubBoardKeyClass;

import groovy.transform.ToString;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ClubBoardKeyClass.class)
@ToString(excludes = "imgaes")
public class Club_Board extends BaseEntity{
    @Id
    @JoinColumn(name = "CLUB_CODE")
    @Column(nullable = false, name = "CLUB_CODE", length = 10)
    private String clubCode;

    @Id
    @Column(nullable = false, name = "BOARD_NO")
    private int boardNo;

    @Column(name = "BOARD_TYPE", length = 20)
    private String boardType;

    @Column(name = "BOARD_TEXT", columnDefinition = "TEXT")
    private String boardText;

    @JoinColumn(name = "MEM_ID")
    @Column(name = "MEM_ID", length = 100)
    private String memID;

    @Column(name = "DELETE_FLAG")
    private Boolean DELETE_FLAG;

    public void change(String type, String text){
        this.boardType = type;
        this.boardText = text;
    }

    @OneToMany(mappedBy = "clubBoard", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    private Set<Club_Board_Image> images = new HashSet<>();

    public void addImage(String uuid, String fileName){
        Club_Board_Image image = Club_Board_Image.builder()
                                 .uuid(uuid)
                                 .boardImage(fileName)
                                 .clubBoard(this)
                                 .ord(images.size())
                                 .build();
        images.add(image);
    }

    public void clearImage() {
        images.forEach(boardImage -> boardImage.changeImgae(null));
        this.images.clear();
    }

}
