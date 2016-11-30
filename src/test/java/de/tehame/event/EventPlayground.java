package de.tehame.event;

import java.util.HashMap;

import de.tehame.photo.meta.PhotoMetadaten;

/**
 * -- Playground --
 * Verwaltet Events. 
 */
public class EventPlayground {
	
	public HashMap<String, Event> events = new HashMap<>();
	
	public void speichereEvent(Event event) {
		this.events.put(event.getUuid(), event);
	}
	
	public Event sucheEvent(String uuid) {
		return this.events.get(uuid);
	}
	
	public PhotoMetadaten bestimmeEventGeoUndZeit(PhotoMetadaten metadaten) {
		final double lat = metadaten.getLatitude();
		final double lon = metadaten.getLongitude();
		
		// TODO es müssen nicht alle Events abgefragt werden, es reichen die, 
		// mit dem entsprechenden Alter wie in den Metadaten des Photos
		for (Event event : this.events.values()) {
			
			// Folgendes muss mit in das Query
			// Sind die Metadaten zu alt oder neu für das Event?
			if (metadaten.getAufnahmeZeitpunkt() <= event.getBegins() - Event.DIFFERENZ_SEKUNDEN 
					|| metadaten.getAufnahmeZeitpunkt() >= event.getEnds() + Event.DIFFERENZ_SEKUNDEN) {
				continue;
			}
			
			// TODO vielleicht geht das mit mongodb direkt?
			final double diffLat = lat - event.getLatitudeCenter();
			final double diffLon = lon - event.getLongitudeCenter();
			final double distance = Math.sqrt(diffLat * diffLat + diffLon * diffLon);
			
			// Liegt der Ort im Umkreis des Events?
			double radius = event.getRadius();
			if (distance <= radius) {
				// Übernehme die Event UUID.
				metadaten.setEventUuid(event.getUuid());
				
				// Berechne den Mittelpunkt neu
				event.berechneNeuenMittelpunkt(lon, lat);
				
				// Bei Bedarf erweitere den Radius um X
				// Photos die am Rand des Umkreises gemacht werden, 
				// sollen diesen vergrößern
				// Achtung: Hier wird getan, als seien es Meter
				if (distance + Event.RADIUS_ERWEITERUNG_WINKEL > event.getRadius()) {
					event.setRadius(distance + Event.RADIUS_ERWEITERUNG_WINKEL);
				}
				
				// Erweitere den zeitlichen Rahmen bei Bedarf
				if (metadaten.getAufnahmeZeitpunkt() - Event.DIFFERENZ_SEKUNDEN < event.getBegins()) {
					event.setBegins(metadaten.getAufnahmeZeitpunkt() - Event.DIFFERENZ_SEKUNDEN);
				}
				
				if (metadaten.getAufnahmeZeitpunkt() + Event.DIFFERENZ_SEKUNDEN > event.getEnds()) {
					event.setEnds(metadaten.getAufnahmeZeitpunkt() + Event.DIFFERENZ_SEKUNDEN);
				}
				
				this.speichereEvent(event);
				
				return metadaten;
			}
		}
		
		// Wenn nichts gefunden wird muss ein neues Event geschaffen werden
		Event event = new Event(lon, lat, metadaten.getAufnahmeZeitpunkt());
		this.speichereEvent(event);
		
		metadaten.setEventUuid(event.getUuid());
		
		return metadaten;
	}
	
	public PhotoMetadaten bestimmeEventGeo(PhotoMetadaten metadaten) {
		final double lat = metadaten.getLatitude();
		final double lon = metadaten.getLongitude();
		
		// TODO es müssen nicht alle Events abgefragt werden, es reichen die, 
		// mit dem entsprechenden Alter wie in den Metadaten des Photos
		for (Event event : this.events.values()) {
			
			// TODO vielleicht geht das mit mongodb direkt?
			final double diffLat = lat - event.getLatitudeCenter();
			final double diffLon = lon - event.getLongitudeCenter();
			final double distance = Math.sqrt(diffLat * diffLat + diffLon * diffLon);
			
			// Liegt der Ort im Umkreis des Events?
			double radius = event.getRadius();
			if (distance <= radius) {
				// Übernehme die Event UUID.
				metadaten.setEventUuid(event.getUuid());
				
				// Berechne den Mittelpunkt neu
				event.berechneNeuenMittelpunkt(lon, lat);
				
				// Bei Bedarf erweitere den Radius um X
				// Photos die am Rand des Umkreises gemacht werden, 
				// sollen diesen vergrößern
				// Achtung: Hier wird getan, als seien es Meter
				if (distance + Event.RADIUS_ERWEITERUNG_WINKEL > event.getRadius()) {
					event.setRadius(distance + Event.RADIUS_ERWEITERUNG_WINKEL);
					this.speichereEvent(event);
				}
				
				return metadaten;
			}
		}
		
		// Wenn nichts gefunden wird muss ein neues Event geschaffen werden
		Event event = new Event(lon, lat, metadaten.getAufnahmeZeitpunkt());
		this.speichereEvent(event);
		
		metadaten.setEventUuid(event.getUuid());
		
		return metadaten;
	}
}
