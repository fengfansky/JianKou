package com.arrownock.appo.desk;

import java.util.List;

public class Group {
	private String id = null;
	private String name = null;
	private List<String> tags = null;
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
