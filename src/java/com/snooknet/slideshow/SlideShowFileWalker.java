package com.snooknet.slideshow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public class SlideShowFileWalker {
	public static void main(String[] args) {
		System.out.println("Starting search for media files from " + Config.getMediaRootDir());
		long start = System.currentTimeMillis();

		IOFileFilter filenameFilter = new SuffixFileFilter(Config.getMediaSuffixes(), IOCase.INSENSITIVE);
		IOFileFilter dirFilter = new NotFileFilter(new WildcardFileFilter(Config.getExclusionWildcards()));
		Collection<File> files = FileUtils.listFiles(new File(Config.getMediaRootDir()), filenameFilter, dirFilter);

		System.out.println("Collected " + files.size() + " files.");

		ArrayList<MediaFile> mediaFiles = new ArrayList<MediaFile>(files.size());
		int count = 0;
		for (File file : files) {
			MediaFile mediaFile = getMediaFile(file);
			if (mediaFile != null) {
				mediaFiles.add(mediaFile);
				if (count++ % 1000 == 999) {
					System.out.println("Found " + count + " files in " + ((System.currentTimeMillis() - start) / 60000) + " mins");
				}
			}
		}

		saveFileList(mediaFiles);

		System.out.println("Total files: " + mediaFiles.size() + " in " + ((System.currentTimeMillis() - start) / 60000) + " mins");
	}

	private static MediaFile getMediaFile(File file) {

		MediaFile mediaFile = null;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
			if (directory != null) {
				Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				if (date != null) {
					mediaFile = new MediaFile(MediaFile.MediaFileType.IMAGE, file.getAbsolutePath(), date.getTime());

					GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
					if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
						GeoLocation location = gpsDirectory.getGeoLocation();
						mediaFile.setLatitude(location.getLatitude());
						mediaFile.setLongitude(location.getLongitude());
					}

					ExifIFD0Directory dirIFD0 = metadata.getDirectory(ExifIFD0Directory.class);
					if (dirIFD0 != null) {
						mediaFile.setCameraModel(dirIFD0.getString(ExifIFD0Directory.TAG_MODEL));
					}
				}
			} else {
				System.out.println("Can't find EXIF data for " + file.getAbsolutePath());
			}
		} catch (ImageProcessingException e2) {
			System.out.println("Exception with file " + file.getAbsolutePath() + ": " + e2.getMessage());
		} catch (Exception e2) {
			System.out.println("Exception with file " + file.getAbsolutePath());
			e2.printStackTrace();
			return null;
		}

		return mediaFile;
	}

	// private static long getFileCreationDate(File file) {
	// Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
	// BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
	// FileTime creationTime = attr.creationTime();
	// return creationTime.toMillis();
	// }

	private static void saveFileList(Collection<MediaFile> files) {
		Type listType = new TypeToken<Collection<MediaFile>>() {
		}.getType();
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(Config.getMediaListFilename()));
			Gson gson = new Gson();
			gson.toJson(files, listType, new JsonWriter(bw));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
