package com.kproject.imageloader.models;

public class Bookmark {
	private int id;
	private String title;
	private String url;
	
	public Bookmark() {}
	
	public Bookmark(String title, String url) {
		this.title = title;
		this.url = url;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
	
}
