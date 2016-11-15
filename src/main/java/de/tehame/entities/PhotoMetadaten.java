package de.tehame.entities;

public class PhotoMetadaten {
	
	/**
	 * Das Aufnahmedatum und Uhrzeit.
	 */
	private final String aufnahmeZeitpunkt;
	
	/**
	 * GPS Längengrad (Grad Ost). -1, wenn unbekannt.
	 */
	private final double longitude;
	
	/**
	 * GPS Breitengrad (Grad Nord). -1, wenn unbekannt.
	 */
	private final double latitude;
	
	/**
	 * Breite in Pixel.
	 */
	private final int breite;
	
	/**
	 * Höhe in Pixel.
	 */
	private final int hoehe;
	
	/**
	 * @param dateTimeOriginal Datum und Uhrzeit der Aufnahme.
	 * @param longitude Längengrad.
	 * @param latitude Breitengrad.
	 * @param breite Breite in Pixel.
	 * @param hoehe Höhe in Pixel.
	 */
	public PhotoMetadaten(String dateTimeOriginal, double longitude, double latitude, int breite, int hoehe) {
		this.aufnahmeZeitpunkt = dateTimeOriginal;
		this.longitude = longitude;
		this.latitude = latitude;
		this.breite = breite;
		this.hoehe = hoehe;
	}


	public String getDateTimeOriginal() {
		return aufnahmeZeitpunkt;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public int getHoehe() {
		return hoehe;
	}


	public int getBreite() {
		return breite;
	}
	
	@Override
	public String toString() {
		return super.toString() 
				+ " [aufnahmeZeitpunkt=" + aufnahmeZeitpunkt 
				+ ", breite=" + breite
				+ ", hoehe=" + hoehe
				+ ", longitude=" + longitude 
				+ ", latitude=" + latitude 
				+ "]";
	}
}
