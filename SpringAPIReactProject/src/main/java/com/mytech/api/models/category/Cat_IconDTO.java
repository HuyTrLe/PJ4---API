package com.mytech.api.models.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Cat_IconDTO {
	private Long id;
	private String path;

	public Cat_IconDTO(Cat_IconDTO icon) {
		this.id = icon.getId();
		this.path = icon.getPath();
	}
}
