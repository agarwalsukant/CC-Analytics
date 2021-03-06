package com.psl.cc.analytics.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Document(collection = "users")
public class CCUser extends Audit {
	@Id
	private String id;
	private String name;
	@Indexed(unique = true, direction = IndexDirection.DESCENDING)
	private String username;
	private String password;
	private List<Role> roles;
	private boolean active;

	public String getId() {
		return id;
	}

//	public void setId(String id) {
//		this.id = id;
//	}

	public String getUsername() {
		return username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<GrantedAuthority> getUserAuthority() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		roles.forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
		return grantedAuthorities;
	}

	public CCUser(String name, String username, String password, List<Role> roles, boolean active) {
		super();

		this.name = name;
		this.username = username;
		this.password = password;
		this.roles = roles;
		this.active = active;
	}

	public CCUser() {
		super();
	}

}
