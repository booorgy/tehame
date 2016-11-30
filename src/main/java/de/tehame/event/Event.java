package de.tehame.event;

import java.util.UUID;

/**
 * Events beschreiben ein Ereignis, bei dem ein 
 * oder mehrere Personen beteiligt sind und Photos machen.
 * Photos zu einem Event bilden eine geographische Punktwolke mit einem Mittelpunkt.
 * Um den Mittelpunkt wird ein Kreis gespannt und neue Photos die in diesem Kreis liegen
 * gehören zu dem Event, außer dieses Event liegt zu weit in der Vergangenheit oder das Photo
 * liegt zu weit in der Vergangenheit. 
 */
public class Event {
	
	/**
	 * Wenn ein neues Event erstellt wird, dann definiert das Photo den zeitlichen
	 * Mittelpunkt. Diese Differenz hier spannt einen zeitlichen Rahmen (+/-) auf,
	 * in dem ein anderes Photo gemacht sein muss, um zu dem gleichen Event zu
	 * gehören.
	 */
	public static final long DIFFERENZ_SEKUNDEN = 3600L;
	
	/**
	 * Wenn ein neues Event erstellt wird, dann definiert das Photo den räumlichen
	 * Mittelpunkt. Dieser Radius hier spannt einen *initialen* Umkreis auf,
	 * in dem ein anderes Photo gemacht sein muss, um zu dem gleichen Event zu
	 * gehören.
	 */
	public static final double RADIUS_WINKEL = 100d; // TODO 100 ist natürlich viel zu viel, hier geht es um das Winkelmaß
	
	/**
	 * Wenn das Photo nahe am Rand innerhalb eines Event-Umkreises gemacht wird,
	 * dann wird der Radius um diesen Winkel hier erweitert,
	 * wenn der Umkreis um das Bild mit diesem Radius hier aus dem Umkreis des
	 * Events herausragt. Der Umkreis des Events muss dann wachsen.
	 */
	public static final double RADIUS_ERWEITERUNG_WINKEL = 10d; // TODO 10 ist natürlich viel zu viel, hier geht es um das Winkelmaß

	/**
	 * UUID des Events.
	 */
	private String uuid = null;
	
	/**
	 * Das älteste Photo bestimmt diesen UNIX Timestamp.
	 */
	private long begins = -1L;
	
	/**
	 * Das jüngste Photo bestimmt diesen UNIX Timestamp.
	 */
	private long ends = -1L;
	
	/**
	 * Die Summe aller Längengrade. Wird benötigt um den Mittelpunkt zu berechnen.
	 */
	private double longitudeSum = 0L;
	
	/**
	 * Die Summe aller Breitengrade. Wird benötigt um den Mittelpunkt zu berechnen.
	 */
	private double latitudeSum = 0L;
	
	/**
	 * Längengrad des Mittelpunkts.
	 */
	private double longitudeCenter = 0L;
	
	/**
	 * Breitengrad des Mittelpunkts.
	 */
	private double latitudeCenter = 0L;
	
	/**
	 * Die Anzahl der Photos wird benötigt, um den Mittelpunkt zu berechnen.
	 */
	private int anzahlPhotos = 0;
	
	/**
	 * Radius des Events um den Mittelpunkt. Sollte etwas größer sein,
	 * als die maximale Distanz zwischen dem Mittelpunkt und aller Punkte in der Wolke.
	 */
	private double radius = 0L;
	
	/**
	 * Die Metadaten zu einem neuen Event können aus dem Photo übernommen werden.
	 * @param lon Längengrad.
	 * @param lat Breitengrad.
	 * @param aufnahmeZeitpunkt UNIX Timestamp.
	 */
	public Event(double lon, double lat, long aufnahmeZeitpunkt) {
		this.uuid = UUID.randomUUID().toString();
		this.latitudeCenter = lat;
		this.longitudeCenter = lon;
		this.latitudeSum = lat;
		this.longitudeSum = lon;
		this.anzahlPhotos = 1;
		this.radius = RADIUS_WINKEL;
		this.ends = aufnahmeZeitpunkt + DIFFERENZ_SEKUNDEN;
		this.begins = aufnahmeZeitpunkt - DIFFERENZ_SEKUNDEN;
	}
	
	public void berechneNeuenMittelpunkt(double lon, double lat) {
		this.latitudeSum += lat;
		this.longitudeSum += lon;
		this.anzahlPhotos++;
		this.latitudeCenter = this.latitudeSum / this.anzahlPhotos;
		this.longitudeCenter = this.longitudeSum / this.anzahlPhotos;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getBegins() {
		return begins;
	}

	public void setBegins(long begins) {
		this.begins = begins;
	}

	public long getEnds() {
		return ends;
	}

	public void setEnds(long ends) {
		this.ends = ends;
	}

	public double getLongitudeSum() {
		return longitudeSum;
	}

	public void setLongitudeSum(double longitudeSum) {
		this.longitudeSum = longitudeSum;
	}

	public double getLatitudeSum() {
		return latitudeSum;
	}

	public void setLatitudeSum(double latitudeSum) {
		this.latitudeSum = latitudeSum;
	}

	public double getLongitudeCenter() {
		return longitudeCenter;
	}

	public void setLongitudeCenter(double longitudeCenter) {
		this.longitudeCenter = longitudeCenter;
	}

	public double getLatitudeCenter() {
		return latitudeCenter;
	}

	public void setLatitudeCenter(double latitudeCenter) {
		this.latitudeCenter = latitudeCenter;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public int getAnzahlPhotos() {
		return anzahlPhotos;
	}

	public void setAnzahlPhotos(int anzahlPhotos) {
		this.anzahlPhotos = anzahlPhotos;
	}
}
