package com.googlecode.freebasegetter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class FreebaseGetter {
	private CloseableHttpClient client;
	//
	// Basic URL and parameters. In this application, we use Freebase's Topic
	// API to acquire name and description of entities.
	//
	private final static String serverURL = "https://www.googleapis.com/freebase/v1/topic/m/";
	private final static String properties = "?filter=/type/object/name&filter=/common/topic/description&lang=all&key=AIzaSyCO_4rBgkyS-emarnRGEVJHaus8ddYmKfk";
	private static HashMap<String, JSONArray> i18nNameMap;
	private static HashMap<String, JSONArray> i18nDescriptionMap;

	public FreebaseGetter() {
		client = HttpClients.createMinimal();
		i18nNameMap = new HashMap<String, JSONArray>();
		i18nDescriptionMap = new HashMap<String, JSONArray>();
	}

	public void getEntity(String mid) throws IOException, JSONException {
		//
		// Execute request and parse response into a JSON object;
		//
		CloseableHttpResponse response = client.execute(new HttpGet(serverURL
				+ mid + properties));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		JSONObject input = new JSONObject(out.toString("utf-8"));
		//
		// Some entities have no description, which is normal. But some
		// descriptions have no name? LOL...
		//
		JSONArray names = input.getJSONObject("property")
				.getJSONObject("/type/object/name").getJSONArray("values");
		JSONArray descriptions = null;
		if (input.getJSONObject("property").has("/common/topic/description") == true) {
			descriptions = input.getJSONObject("property")
					.getJSONObject("/common/topic/description")
					.getJSONArray("values");
		}
		JSONArray nameArray;
		JSONArray descriptionArray;
		//
		// Store names according to their languages.
		//
		int namesLength = names.length();
		for (int i = 0; i < namesLength; i++) {
			String language = names.getJSONObject(i).getString("lang");
			if (i18nNameMap.get(language) == null) {
				nameArray = new JSONArray();
				i18nNameMap.put(language, nameArray);
			} else {
				nameArray = i18nNameMap.get(language);
			}
			JSONObject toAdd = new JSONObject();
			toAdd.put("id", mid);
			toAdd.put("name", names.getJSONObject(i).getString("value"));
			nameArray.put(toAdd);
		}
		//
		// Store descriptions according to their languages.
		//
		if (descriptions==null) return;
		int descriptonsLength = descriptions.length();
		for (int i = 0; i < descriptonsLength; i++) {
			String language = descriptions.getJSONObject(i).getString("lang");
			if (i18nDescriptionMap.get(language) == null) {
				descriptionArray = new JSONArray();
				i18nDescriptionMap.put(language, descriptionArray);
			} else {
				descriptionArray = i18nDescriptionMap.get(language);
			}
			JSONObject toAdd = new JSONObject();
			toAdd.put("id", mid);
			toAdd.put("description", descriptions.getJSONObject(i).getString("value"));
			descriptionArray.put(toAdd);
		}
	}

	public static void main(String[] args) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new FileReader(
				"entities.txt"));
		ArrayList<String> entities = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			entities.add(line);
		}
		reader.close();
		FreebaseGetter freebaseGetter = new FreebaseGetter();
		int counter = 0;
		for (String entity : entities) {
			System.out.println("Getting entity No." + (++counter) + ": "+entity);
			String id = entity.split("\t")[1];
			freebaseGetter.getEntity(id);
		}
		Set<String> languagedNameSet = i18nNameMap.keySet();
		for (String language : languagedNameSet){
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("output/" + language + "_names.txt"), "utf-8"));
			writer.write(i18nNameMap.get(language).toString(4));
			writer.close();
		}
		Set<String> languagedDescriptionSet = i18nDescriptionMap.keySet();
		for (String language : languagedDescriptionSet){
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("output/" + language + "_descriptions.txt"), "utf-8"));
			writer.write(i18nDescriptionMap.get(language).toString(4));
			writer.close();
		}
	}
}
