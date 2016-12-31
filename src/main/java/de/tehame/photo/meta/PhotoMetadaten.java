package de.tehame.photo.meta;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.rekognition.model.Label;

public class PhotoMetadaten {
	
	/**
	 * User UUID.
	 */
	private String userUuid = null;
	
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
	 * Dinge, die auf dem Photo zu sehen sind.
	 */
	private String[] labels = null;
	
	/**
	 * Labels als Text.
	 */
	private String labelsTooltip = null;
	
	/**
	 * @param userUuid User UUID.
	 * @param aufnahmeZeitpunkt UNIX Timestamp der Aufnahme.
	 * @param longitude Längengrad.
	 * @param latitude Breitengrad.
	 * @param breite Breite in Pixel.
	 * @param hoehe Höhe in Pixel.
	 * @param s3bucket S3 Bucket Name.
	 * @param s3key S3 Key.
	 * @param zugehoerigkeit 0 = Privat, 1 = Familie, 2 = Freunde, 3 = Öffentlich
	 * @param labels Amazon Rekognition Labels.
	 */
	public PhotoMetadaten(String userUuid, long aufnahmeZeitpunkt, double longitude, 
			double latitude, int breite, int hoehe, String s3bucket, String s3key, int zugehoerigkeit, 
			String[] labels) {
		
		this.userUuid = userUuid;
		this.aufnahmeZeitpunkt = aufnahmeZeitpunkt;
		this.longitude = longitude;
		this.latitude = latitude;
		this.breite = breite;
		this.hoehe = hoehe;
		this.s3bucket = s3bucket;
		this.s3key = s3key;
		this.zugehoerigkeit = zugehoerigkeit;
		this.labels = labels;
		
		// Konstruiere Tooltip für Fotos
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.labels.length; i++) {
			sb.append(this.labels[i]);
			
			if (i != this.labels.length - 1) {
				sb.append(", ");
			}
		}
		this.labelsTooltip = sb.toString();
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

	public int getZugehoerigkeit() {
		return zugehoerigkeit;
	}

	public String getUserUuid() {
		return userUuid;
	}
	
	public String[] getLabels() {
		return this.labels;
	}
	
	@Override
	public String toString() {
		return super.toString() 
				+ " [aufnahmeZeitpunkt=" + aufnahmeZeitpunkt 
				+ ", useruuid=" + userUuid
				+ ", breite=" + breite
				+ ", hoehe=" + hoehe
				+ ", longitude=" + longitude 
				+ ", latitude=" + latitude 
				+ ", s3bucket=" + s3bucket 
				+ ", s3key=" + s3key 
				+ ", eventUuid=" + eventUuid
				+ ", zugehoerigkeit=" + zugehoerigkeit 
				+ ", labels=" + Arrays.toString(labels)
				+ "]";
	}

	public String getLabelsTooltip() {
		return labelsTooltip;
	}
}
