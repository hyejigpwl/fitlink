package com.lec.packages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@ToString(exclude = "clubBoard")
public class Club_Board_Image extends BaseEntity implements Comparable<Club_Board_Image>{
	
	@Id
	private String uuid;

    @Column(nullable = false, name = "BOARD_IMAGE")
    private String boardImage;

    private int ord;

    @Column(name = "DELETE_FLAG")
    private Boolean deleteFlag;

    @ManyToOne
    private Club_Board clubBoard;

    @Override
    public int compareTo(Club_Board_Image o) {
        if(this.boardImage == o.boardImage) {
            return 0;
        } else {
            return -1;
        }
    }

    public void changeImgae(Club_Board club_Board) {
        this.clubBoard = club_Board;
    }
}
