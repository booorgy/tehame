package de.tehame;

/**
 * Umgebungsvariablen.
 * Debian: Siehe /etc/environment 
 */
public class TehameProperties {
	public static final String PHOTO_BUCKET 		= System.getenv("TEHAME_PHOTO_BUCKET");
	public static final String THUMBNAIL_BUCKET 	= System.getenv("TEHAME_THUMBNAIL_BUCKET");
	public static final String MONGO_DB_URL 		= System.getenv("TEHAME_MONGO_DB_URL");
	
	public static final String HTTP_SERVER_URL = "http://localhost:8080/"; // TODO ???
	public static final String IMAGE_CALLBACK_URL_JSF = HTTP_SERVER_URL + 
			"tehame/rest/v1/photos/www/";
	
}
