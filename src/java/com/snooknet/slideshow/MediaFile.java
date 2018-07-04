package com.snooknet.slideshow;

import org.joda.time.LocalDateTime;

public class MediaFile {
	private MediaFileType type;

	private String filename;

	private long creationDate;

	private double latitude;

	private double longitude;

	private String location;

	private String cameraModel;

	public MediaFile(MediaFileType type, String filename, long creationDate) {
		this.type = type;
		this.filename = filename;
		this.creationDate = creationDate;
	}

	public MediaFileType getType() {
		return type;
	}

	public void setType(MediaFileType type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCameraModel() {
		return cameraModel;
	}

	public void setCameraModel(String cameraModel) {
		this.cameraModel = cameraModel;
	}

	public enum MediaFileType {
		IMAGE
	}

	@Override
	public String toString() {
		return "MediaFile [creationDate=" + new LocalDateTime(creationDate) + ", filename=" + filename + "]";
	}

}
