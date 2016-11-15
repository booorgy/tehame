package de.tehame;

public class Properties {
	public static String MONGO_URL = System.getenv("MONGO_DB_URL");
	
	/**
	 * Um zu prüfen, ob ENV Variablen verfügbar sind. 
	 * Debian: Siehe /etc/environment
	 */
	public static void main(String [] _) {
		System.out.println(MONGO_URL);
	}
}