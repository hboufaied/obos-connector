package com.mstools.connector.obos.model;

public class UploadResult {
	
	private String fileUrl;

	public UploadResult() {

	}

	public UploadResult(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

}