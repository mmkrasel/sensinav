package edu.ewubd.cse.sensinav.util;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Remote {
	private final String TAG = "@Remote";
	private static final Remote instance = new Remote();
	private Remote() {}
	public static Remote getInstance() {
		return instance;
	}

	public JSONObject httpRequest(String url, JSONObject params) {
		JSONObject jObj = null;
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		try {
			System.out.println(TAG+"=>"+ url);
			URL rlc = new URL(url);
			urlConnection = (HttpURLConnection) rlc.openConnection();
			if(params == null) {
				urlConnection.setRequestMethod("GET");
			}else {
				urlConnection.setRequestMethod("POST");
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setDoOutput(true);
				// Write the JSON payload to the request body
				OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
				writer.write(params.toString());
				writer.flush();
				writer.close();
			}
			InputStream is = urlConnection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			is.close();
			String json = sb.toString();
			System.out.println(TAG+"=>"+ json);
			jObj = new JSONObject(json);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		} finally {
			// Close the connections and readers
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jObj;
	}
}
