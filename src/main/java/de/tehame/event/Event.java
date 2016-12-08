package de.tehame.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.User;

/**
 * Events beschreiben ein Ereignis, bei dem ein 
 * oder mehrere Personen beteiligt sind und Photos machen.
 * Photos zu einem Event bilden eine geographische Punktwolke mit einem Mittelpunkt.
 * Um den Mittelpunkt wird ein Kreis gespannt und neue Photos die in diesem Kreis liegen
 * gehören zu dem Event, außer dieses Event liegt zu weit in der Vergangenheit oder das Photo
 * liegt zu weit in der Vergangenheit. 
 */
@Entity(name = "event")
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

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
	public static final double RADIUS_INITIAL_METER = 100d;
	
	/**
	 * Wenn das Photo nahe am Rand innerhalb eines Event-Umkreises gemacht wird,
	 * dann wird der Radius um diese Meter hier erweitert,
	 * wenn der Umkreis um das Bild mit diesem Radius hier aus dem Umkreis des
	 * Events herausragt. Der Umkreis des Events muss dann wachsen.
	 */
	public static final double RADIUS_ERWEITERUNG_METER = 10d;

	/**
	 * UUID des Events.
	 */
	@Id
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
	 * Radius des Events um den Mittelpunkt in Metern. Sollte etwas größer sein,
	 * als die maximale Distanz zwischen dem Mittelpunkt und aller Punkte in der Wolke.
	 */
	private double radius = 0L;
	
	/**
	 * Liste mit den Urls zu den Bildern des Events.
	 */
	@Transient
	private ArrayList<String> photoUrls = new ArrayList<String>();
	
	/**
	 * Bidirektional N:N, die User zu diesem Event.
	 */
	@ManyToMany(mappedBy="events")
	private List<User> users;
	
	/**
	 * @deprecated Standardkonstruktor für JPA.
	 */
	public Event() { super(); }
	
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
		this.radius = RADIUS_INITIAL_METER;
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
	 * @return Die Distanz zwischen dem Event Mittelpunkt und der Photo Geolocation in Metern.
	 */
	protected double berechneDistanzZumMittelpunkt(PhotoMetadaten metadaten) {
		final double distanz = this.haversine(
				/* Koordinate 1 */ metadaten.getLatitude(), metadaten.getLongitude(), 
				/* Koordinate 2 */ this.getLatitudeCenter(), this.getLongitudeCenter());
		return distanz;
	}
	
	/**
	 * Quelle: http://stackoverflow.com/questions/18861728/calculating-distance-between-two-points-represented-by-lat-long-upto-15-feet-acc
	 * 
	 * Weitere Infos zum Thema:
	 * Google verwendet vermutlich WGS84 als Koordinaten Referenz System (wie GPS):
	 * http://stackoverflow.com/questions/1676940/google-maps-spatial-reference-system
	 * 
	 * "WGS 84 is the reference coordinate system used by the Global Positioning System"
	 * (Quelle: https://en.wikipedia.org/wiki/World_Geodetic_System)
	 * 
	 * http://api.mongodb.com/java/3.2/com/mongodb/client/model/geojson/NamedCoordinateReferenceSystem.html
	 * 
	 * http://gis.stackexchange.com/questions/54073/what-is-crs84-projection
	 * "CRS:84 is equivalent to EPSG:4326 - ie, basic WGS84 degrees"
	 * 
	 * http://mapserver.org/ogc/wms_server.html#coordinate-systems-and-axis-orientation
	 * "CRS:84 (WGS 84 longitude-latitude)"
	 * 
	 * @param lat1 Latitude 1 in Grad.
	 * @param lng1 Longitude 1 in Grad.
	 * @param lat2 Latitude 2 in Grad.
	 * @param lng2 Longitude 2 in Grad.
	 * 
	 * @return Distanz in Metern.
	 */
	protected double haversine(double lat1, double lng1, double lat2, double lng2) {
	    int r = 6371 * 1000; // Durchschnittlicher Radius der Erde in Metern
	    double dLat = Math.toRadians(lat2 - lat1);
	    double dLon = Math.toRadians(lng2 - lng1);
	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
	       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) 
	      * Math.sin(dLon / 2) * Math.sin(dLon / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = r * c;
	    return d;
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
		if (distance + Event.RADIUS_ERWEITERUNG_METER > this.getRadius()) {
			this.setRadius(distance + Event.RADIUS_ERWEITERUNG_METER);
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
	
	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + anzahlPhotos;
		result = prime * result + (int) (begins ^ (begins >>> 32));
		result = prime * result + (int) (ends ^ (ends >>> 32));
		long temp;
		temp = Double.doubleToLongBits(latitudeCenter);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latitudeSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitudeCenter);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitudeSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (anzahlPhotos != other.anzahlPhotos)
			return false;
		if (begins != other.begins)
			return false;
		if (ends != other.ends)
			return false;
		if (Double.doubleToLongBits(latitudeCenter) != Double.doubleToLongBits(other.latitudeCenter))
			return false;
		if (Double.doubleToLongBits(latitudeSum) != Double.doubleToLongBits(other.latitudeSum))
			return false;
		if (Double.doubleToLongBits(longitudeCenter) != Double.doubleToLongBits(other.longitudeCenter))
			return false;
		if (Double.doubleToLongBits(longitudeSum) != Double.doubleToLongBits(other.longitudeSum))
			return false;
		if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
			return false;
		if (users == null) {
			if (other.users != null)
				return false;
		} else if (!users.equals(other.users))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public ArrayList<String> getPhotoUrls() {
		return photoUrls;
	}

	public void setPhotoUrls(ArrayList<String> photoUrls) {
		this.photoUrls = photoUrls;
	}
	
	
}
