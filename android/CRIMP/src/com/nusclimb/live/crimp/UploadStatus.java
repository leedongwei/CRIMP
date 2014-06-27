package com.nusclimb.live.crimp;

public enum UploadStatus {
	PAUSED("Paused."),
	DOWNLOAD("Downloading previous score."), 
	DOWNLOAD_WAITING("D/L failed. Waiting to retry."), 
	UPLOAD("Uploading score."), 
	UPLOAD_WAITING("U/L failed. Waiting to retry."), 
	FINISHED("Finished."),
	ERROR_DOWNLOAD("Download error. Click to remedy."),
	ERROR_UPLOAD("Upload error. Click to remedy."),
	ERROR_NO_NETWORK("No Network.");
	
	private String string;
	
	private UploadStatus(String string){
		this.string = string;
	}
	
	@Override
	public String toString(){
		return this.string;
	}
}
