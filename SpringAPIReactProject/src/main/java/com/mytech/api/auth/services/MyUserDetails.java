package com.mytech.api.auth.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mytech.api.models.user.User;

import lombok.Setter;

@Setter
public class MyUserDetails implements OAuth2User, UserDetails {

	private static final long serialVersionUID = 1L;

	private long id;

	private String username;

	private String email;

	@JsonIgnore
	private String password;

	private Boolean isEnabled;

	private Collection<? extends GrantedAuthority> authorities;

	private Map<String, Object> attributes;

	public MyUserDetails(long id, String username, String email, String password, boolean isEnabled,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.isEnabled = isEnabled;
		this.authorities = authorities;
	}

	public static MyUserDetails build(User user) {
		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		return new MyUserDetails(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.isEnabled(), authorities);

	}

	public static MyUserDetails create(User user, Map<String, Object> attributes) {
		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		MyUserDetails myUserDetails = new MyUserDetails(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.isEnabled(), authorities);
		myUserDetails.setAttributes(attributes);
		return myUserDetails;
	}

	public long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getName() {
		return String.valueOf(id);
	}
}
