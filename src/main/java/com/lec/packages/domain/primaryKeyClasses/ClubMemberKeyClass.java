package com.lec.packages.domain.primaryKeyClasses;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClubMemberKeyClass implements Serializable{
	
	private String memId;
    private String clubCode;

}
