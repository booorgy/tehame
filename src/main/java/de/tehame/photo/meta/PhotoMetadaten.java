package de.tehame.photo.meta;

public class PhotoMetadaten {
	
	/**
	 * Das Aufnahmedatum und Uhrzeit.
	 */
	private final long aufnahmeZeitpunkt;
	
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
	 * ID des Bucket im S3.
	 */
	private final String s3bucket;
	
	/**
	 * Id des Objectes im Bucket im S3.
	 */
	private final String s3key;

	/**
	 * Die UUID des Events, zu dem dieses Bild gehört.
	 */
	private String eventUuid;
	
	/**
	 * Ist es ein Privat, Familien, Freunde oder Public Bild?
	 */
	private final int zugehoerigkeit;
	
	/**
	 * @param aufnahmeZeitpunkt UNIX Timestamp der Aufnahme.
	 * @param longitude Längengrad.
	 * @param latitude Breitengrad.
	 * @param breite Breite in Pixel.
	 * @param hoehe Höhe in Pixel.
	 * @param zugehoerigkeit 0 = Privat, 1 = Familie, 2 = Freunde, 3 = Öffentlich
	 */
	public PhotoMetadaten(long aufnahmeZeitpunkt, double longitude, double latitude, int breite, int hoehe, String s3bucket, String s3key, int zugehoerigkeit) {
		this.aufnahmeZeitpunkt = aufnahmeZeitpunkt;
		this.longitude = longitude;
		this.latitude = latitude;
		this.breite = breite;
		this.hoehe = hoehe;
		this.s3bucket = s3bucket;
		this.s3key = s3key;
		this.zugehoerigkeit = zugehoerigkeit;
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
	
	public long getAufnahmeZeitpunkt() {
		return aufnahmeZeitpunkt;
	}

	public String getS3bucket() {
		return s3bucket;
	}

	public String getS3key() {
		return s3key;
	}

	public String getEventUuid() {
		return eventUuid;
	}

	public void setEventUuid(String eventUuid) {
		this.eventUuid = eventUuid;
	}

	@Override
	public String toString() {
		return super.toString() 
				+ " [aufnahmeZeitpunkt=" + aufnahmeZeitpunkt 
				+ ", breite=" + breite
				+ ", hoehe=" + hoehe
				+ ", longitude=" + longitude 
				+ ", latitude=" + latitude 
				+ ", s3bucket=" + s3bucket 
				+ ", s3key=" + s3key 
				+ ", eventUuid=" + eventUuid
				+ ", zugehoerigkeit=" + zugehoerigkeit 
				+ "]";
	}

	public int getZugehoerigkeit() {
		return zugehoerigkeit;
	}
}
