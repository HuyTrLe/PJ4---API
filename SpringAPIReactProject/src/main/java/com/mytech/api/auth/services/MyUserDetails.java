package com.mytech.api.auth.services;

import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mytech.api.models.user.UserDTO;

public class MyUserDetails implements UserDetails{
	
	private static final long serialVersionUID = 1L;

	private Long id;

	private String username;

	private String email;

	@JsonIgnore
	private String password;
	
	private Boolean isEnabled;

	public MyUserDetails(Long id, String username, String email, String password, boolean isEnabled) {
		this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEnabled = isEnabled;
    }
	 public static MyUserDetails build(UserDTO user) {
	        return new MyUserDetails(
	                user.getId(),
	                user.getUsername(),
	                user.getEmail(),
	                user.getPassword(), 
	        		user.isEnabled());
	        
	        		
	}

	
	 public Long getId() {
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
	  public boolean equals(Object o) {
	    if (this == o)
	      return true;
	    if (o == null || getClass() != o.getClass())
	      return false;
	    MyUserDetails user = (MyUserDetails) o;
	    return Objects.equals(id, user.id);
	  }
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}
}
