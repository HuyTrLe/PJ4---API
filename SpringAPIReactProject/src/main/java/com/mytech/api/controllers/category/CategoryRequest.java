package com.mytech.api.controllers.category;

import com.mytech.api.models.category.CateTypeENum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
	private String name;
	private CateTypeENum type;
	private Long iconId;
	private Long userId;
}
