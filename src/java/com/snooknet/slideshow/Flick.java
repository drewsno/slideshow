package com.snooknet.slideshow;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Flick {

	private static final String CHARSET = "UTF-8";

	private String token;

	private Integer price = null;

	public Flick() {
		token = fetchBearerToken();

		fetchPrice();
	}

	private String fetchBearerToken() {
		Map<String, String> params = new HashMap<>();
		params.put("grant_type", "password");
		params.put("client_id", "le37iwi3qctbduh39fvnpevt1m2uuvz");
		params.put("client_secret", "ignwy9ztnst3azswww66y9vd9zt6qnt");
		params.put("username", "andrew.s@snooknet.com");
		params.put("password", Config.getFlickPassword());

		JSONObject response = post("https://api.flick.energy/identity/oauth/token", params);
		if (response != null) {
			return (String) response.get("id_token");
		}
		return null;
	}

	public void fetchPrice() {
		if (token != null) {
			JSONObject response = get("https://api.flick.energy/customer/mobile_provider/price", getToken());
			if (response != null) {
				JSONObject needle = (JSONObject) response.get("needle");
				setPrice(Math.round(Float.parseFloat((String) needle.get("price"))));
				return;
			}
		}
		setPrice(null);
	}

	private JSONObject post(String url, Map<String, String> params) {
		try {
			StringBuilder postData = new StringBuilder();
			if (params != null) {
				for (Map.Entry<String, String> param : params.entrySet()) {
					if (postData.length() != 0)
						postData.append('&');
					postData.append(URLEncoder.encode(param.getKey(), CHARSET));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), CHARSET));
				}
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Charset", "UTF-8");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

			con.setDoOutput(true);
			con.getOutputStream().write(postDataBytes);

			if (con.getResponseCode() != 200) {
				String error = IOUtils.toString(con.getErrorStream(), "UTF-8");
				System.out.println(error);
				return null;
			}
			InputStream response = con.getInputStream();
			JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(new InputStreamReader(response, "UTF-8"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject get(String url, String token) {
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Charset", "UTF-8");
			if (token != null) {
				con.setRequestProperty("Authorization", "Bearer " + token);
			}

			con.setDoOutput(false);

			if (con.getResponseCode() != 200) {
				String error = IOUtils.toString(con.getErrorStream(), "UTF-8");
				System.out.println(error);
				return null;
			}
			InputStream response = con.getInputStream();
			JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(new InputStreamReader(response, "UTF-8"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Color getColor() {
		// 10 = 0, 50 = 255
		int val = (int) (((float) getPrice() - 10) / 40 * 255);
		if (val < 0) {
			val = 0;
		}
		if (val > 255) {
			val = 255;
		}

		return new Color(val, 255 - val, 255 - Math.abs(val * 2 - 255));
	}
}
