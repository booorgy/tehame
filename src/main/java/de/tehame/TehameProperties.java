package de.tehame;

import de.tehame.photo.PhotosS3;

/**
 * Umgebungsvariablen.
 * Debian: Siehe /etc/environment 
 */
public class TehameProperties {
	public static String MONGO_DB_URL = System.getenv("MONGO_DB_URL");
	public static String HTTP_SERVER_URL = "http://localhost:8080/";
	public static String IMAGE_CALLBACK_URL_JSF = HTTP_SERVER_URL + 
			"tehame/rest/v1/photos/www/";
	
}
