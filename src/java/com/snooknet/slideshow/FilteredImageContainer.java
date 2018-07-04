package com.snooknet.slideshow;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FilteredImageContainer {
	private List<MediaFile> filteredList;

	public FilteredImageContainer() throws FileNotFoundException {
		Type listType = new TypeToken<Collection<MediaFile>>() {
		}.getType();
		Gson gson = new Gson();
		Reader json = new FileReader(Config.getMediaListFilename());
		Collection<MediaFile> files = gson.fromJson(json, listType);

		filteredList = filterFileList(files);

		Collections.shuffle(filteredList);

		for (MediaFile mediaFile : filteredList) {
			System.out.println(mediaFile.getFilename());
			System.out.println(mediaFile.getLatitude() + "," + mediaFile.getLongitude());
		}

		System.out.println("filtered count: " + filteredList.size());
	}

	private static List<MediaFile> filterFileList(Collection<MediaFile> files) {
		final int tolerance = Config.getAgeToleranceDays();
		List<MediaFile> filteredFiles = new ArrayList<MediaFile>();
		for (MediaFile mediaFile : files) {
			if (acceptFile(mediaFile, tolerance)) {
				filteredFiles.add(mediaFile);
			}
		}
		return filteredFiles;
	}

	private static boolean acceptFile(MediaFile mediaFile, int tolerance) {
		LocalDateTime creationDate = new LocalDateTime(mediaFile.getCreationDate());

		LocalDateTime now = new LocalDateTime();

		LocalDateTime creationDateThisYear = creationDate.withYear(now.year().get());

		return Math.abs(Days.daysBetween(now, creationDateThisYear).getDays()) < tolerance;
	}

	public List<MediaFile> getFilteredList() {
		return filteredList;
	}

	public void setFilteredList(List<MediaFile> filteredList) {
		this.filteredList = filteredList;
	}

}
