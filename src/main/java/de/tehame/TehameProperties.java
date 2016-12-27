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
	public static final String EB_URL	 			= System.getenv("TEHAME_EB_URL");
	
	/**
	 * Die allgemein präferierte AWS Region (Irland).
	 */
	public static final Regions REGION = Regions.EU_WEST_1;
	
	/**
	 * Simple Email Service SES gibt es in Frankfurt nicht, aber in Irland. 
	 */
	public static final Regions SES_REGION = Regions.EU_WEST_1;
	
	/**
	 * Rekognition gibt es nicht in Frankfurt, aber in Irland.
	 */
	public static final Regions REKOGNITION_REGION = Regions.EU_WEST_1;
	
	/**
	 * Die maximale Anzahl an Labels, die Rekognition liefern soll.
	 */
	public static final int REKOGNITION_MAX_LABELS = 10;
	
	/**
	 * Labels aus Rekognition müssen diese minimale Sicherheit haben, dass sie korrekt sind.
	 */
	public static final float REKOGNITION_MIN_CONFIDENCE = 77F;
	
	/**
	 * SES: Verifizierte Absenderadresse.
	 */
	public static final String SES_FROM = "schinzel.benjamin@gmail.com"; 
}
