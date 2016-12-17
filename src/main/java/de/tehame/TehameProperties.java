package de.tehame;

import com.amazonaws.regions.Regions;

/**
 * Umgebungsvariablen.
 * Debian: Siehe /etc/environment 
 */
public class TehameProperties {
	public static final String PHOTO_BUCKET 		= System.getenv("TEHAME_PHOTO_BUCKET");
	public static final String THUMBNAIL_BUCKET 	= System.getenv("TEHAME_THUMBNAIL_BUCKET");
	public static final String MONGO_DB_URL 		= System.getenv("TEHAME_MONGO_DB_URL");
	
	/**
	 * Die allgemein präferierte AWS Region (Frankfurt).
	 */
	public static final String REGION = "eu-central-1";
	
	/**
	 * Simple Email Service SES gibt es in Frankfurt nicht, aber in Irland. 
	 */
	public static final Regions SES_REGION = Regions.EU_WEST_1;
	
	/**
	 * SES: Verifizierte Absenderadresse.
	 */
	public static final String SES_FROM = "schinzel.benjamin@gmail.com"; 
	
	public static final String HTTP_SERVER_URL = "http://localhost:8080/"; // TODO ???
	public static final String IMAGE_CALLBACK_URL_JSF = HTTP_SERVER_URL + 
			"tehame/rest/v1/photos/www/";
	
}
