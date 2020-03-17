package com.wangjinyin.study.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

public class User implements Serializable{
	
	 @Id
	 @Column
	private Long id;
	
	 @Column
	private String name;
	
	 @Column
	private String password;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
