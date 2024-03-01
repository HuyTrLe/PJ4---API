package com.mytech.api.models.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mytech.api.models.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private CateTypeENum type;

	@ManyToOne
	@JoinColumn(name = "icon_id")
	private Cat_Icon icon;
	
	@JsonIgnore
	@ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	public Category(String name, CateTypeENum type, Cat_Icon icon, User user) {
		this.name = name;
		this.type = type;
		this.icon = icon;
		this.user = user;
	}
}
