package com.kproject.imageloader.models;

public class Manager implements Comparable<Manager> {
	private String folderName;
	private String folderPath;

	public Manager(String folderName, String folderPath) {
		this.folderName = folderName;
		this.folderPath = folderPath;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getFolderPath() {
		return folderPath;
	}

	@Override
	public int compareTo(Manager managerDir) {
		return folderName.compareToIgnoreCase(managerDir.getFolderName());
	}

}
