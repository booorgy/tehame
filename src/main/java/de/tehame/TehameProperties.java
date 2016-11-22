package de.tehame;

/**
 * Umgebungsvariablen.
 * Debian: Siehe /etc/environment 
 */
public class TehameProperties {
	public static String MONGO_DB_URL = System.getenv("MONGO_DB_URL");
}
