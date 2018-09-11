package com.eveb.saasops.modules.sys.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TreeMenuDto {

	private Long id;
	
	private String label;

	List<PermissonDto> children = new ArrayList<>();
}
