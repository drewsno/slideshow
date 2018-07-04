package com.snooknet.slideshow;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LocationFetcher {

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(3);

	public static void setLocation(final MediaFile file) {
		THREAD_POOL.submit(new Callable<Boolean>() {
			public Boolean call() {
				file.setLocation(getLocation(file.getLatitude(), file.getLongitude()));
				return true;
			}

			private String getLocation(double latitude, double longitude) {
				String location = "";
				if (latitude != 0 && longitude != 0) {
					location = fetchLocation(latitude + "," + longitude);
				}
				return location;
			}

			private String fetchLocation(String location) {
				String url = "https://maps.googleapis.com/maps/api/geocode/json?result_type=sublocality&key=AIzaSyCGDOIhwDAlfFGriqDW4D4AgxRGpBK64HA&latlng=" + location;
				HttpURLConnection connection;
				try {
					connection = (HttpURLConnection) new URL(url).openConnection();
					InputStream response = connection.getInputStream();
					if (connection.getResponseCode() != 200) {
						return "";
					}
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(new InputStreamReader(response, "UTF-8"));
					return (String) ((JSONObject) ((JSONArray) json.get("results")).get(0)).get("formatted_address");
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				return "";
			}
		});
	}
}
