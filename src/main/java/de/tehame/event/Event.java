package de.tehame.event;

import java.util.UUID;

import de.tehame.photo.meta.PhotoMetadaten;

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
	public static final double RADIUS_INITIAL_WINKEL = 100d; // TODO 100 ist natürlich viel zu viel, hier geht es um das Winkelmaß
	
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
	 * Die Metadaten bekommen eine neue Event UUID zugewiesen.
	 * 
	 * @param metadaten Photo Metadaten.
	 */
	public Event(final PhotoMetadaten metadaten) {
		
		if (metadaten.getEventUuid() != null) {
			throw new IllegalArgumentException("Die Metadaten haben bereits eine Event UUID.");
		}
		
		this.uuid = UUID.randomUUID().toString();
		metadaten.setEventUuid(this.uuid);
		
		this.latitudeCenter = metadaten.getLatitude();
		this.longitudeCenter = metadaten.getLongitude();
		this.latitudeSum = metadaten.getLatitude();
		this.longitudeSum = metadaten.getLongitude();
		this.anzahlPhotos = 1;
		this.radius = RADIUS_INITIAL_WINKEL;
		this.ends = metadaten.getAufnahmeZeitpunkt() + DIFFERENZ_SEKUNDEN;
		this.begins = metadaten.getAufnahmeZeitpunkt() - DIFFERENZ_SEKUNDEN;
	}
	
	public void berechneNeuenMittelpunkt(PhotoMetadaten metadaten) {
		this.latitudeSum += metadaten.getLatitude();
		this.longitudeSum += metadaten.getLongitude();
		this.anzahlPhotos++;
		this.latitudeCenter = this.latitudeSum / this.anzahlPhotos;
		this.longitudeCenter = this.longitudeSum / this.anzahlPhotos;
	}
	
	/**
	 * @param metadaten Metadaten.
	 * @return Die Distanz zwischen dem Event Mittelpunkt und der Photo Geolocation.
	 */
	protected double berechneDistanzZumMittelpunkt(PhotoMetadaten metadaten) {
		final double diffLat = metadaten.getLatitude() - this.getLatitudeCenter();
		final double diffLon = metadaten.getLongitude() - this.getLongitudeCenter();
		final double distanceX = Math.sqrt(diffLat * diffLat + diffLon * diffLon);
		return distanceX;
	}

	/**
	 * Wenn die Distanz d der Photo Location zum Mittelpunkt des Events
	 * plus der Radius Erweiterung im Winkelmaß größer ist als der 
	 * aktuelle Radius des Umkreises des Events ist, dann vergrößere
	 * diesen auf diese Distanz d.
	 * 
	 * @param distance Distanz des Photos zum Mittelpunkt.
	 */
	protected void erweitereUmkreis(final double distance) {
		if (distance + Event.RADIUS_ERWEITERUNG_WINKEL > this.getRadius()) {
			this.setRadius(distance + Event.RADIUS_ERWEITERUNG_WINKEL);
		}
	}

	/**
	 * Wenn das Aufnahmedatum des Photos minus der Differenz in Sekunden älter ist,
	 * als der Beginn des Events, dann setze den Beginn des Events auf diesen Zeitpunkt.
	 * Analog dazu: Wenn das Aufnahmedatum des Photos plus der Differenz in Sekunden
	 * neuer ist als das Ende des Events, dann setze das Ende auf diesen Zeitpunkt.
	 * 
	 * @param metadaten Photo Metadaten.
	 */
	protected void erweitereZeitlichenRahmen(final PhotoMetadaten metadaten) {
		if (metadaten.getAufnahmeZeitpunkt() - Event.DIFFERENZ_SEKUNDEN < this.getBegins()) {
			this.setBegins(metadaten.getAufnahmeZeitpunkt() - Event.DIFFERENZ_SEKUNDEN);
		}
		
		if (metadaten.getAufnahmeZeitpunkt() + Event.DIFFERENZ_SEKUNDEN > this.getEnds()) {
			this.setEnds(metadaten.getAufnahmeZeitpunkt() + Event.DIFFERENZ_SEKUNDEN);
		}
	}
	
	/**
	 * Liegt der Aufnahmezeitpunkt im folgenden Bereich?
	 * 
	 *     [----------------------------|---------|----------------------------]
	 *     ^ Beginn - Differenz         ^ Beginn  ^ Ende      Ende + Differenz ^
	 *     
	 * @param metadaten Photo Metadaten.
	 * @return Ob der Aufnahmezeitpunkt zu diesem Event passt.
	 */
	protected boolean istZeitlichPassend(PhotoMetadaten metadaten) {
		return metadaten.getAufnahmeZeitpunkt() > this.getBegins() - Event.DIFFERENZ_SEKUNDEN 
				&& metadaten.getAufnahmeZeitpunkt() < this.getEnds() + Event.DIFFERENZ_SEKUNDEN;
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
