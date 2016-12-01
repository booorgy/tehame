package de.tehame.event;

import java.util.HashMap;

import de.tehame.photo.meta.PhotoMetadaten;

public class Events {
	public HashMap<String, Event> events = new HashMap<>();
	
	public void speichereEvent(Event event) {
		this.events.put(event.getUuid(), event);
	}
	
	public Event sucheEvent(String uuid) {
		return this.events.get(uuid);
	}
	
	/**
	 * Die Metadaten werden einem existierenden oder neuen Event zugeordnet.
	 * 
	 * @param metadaten Photo Metadaten ohne Event UUID.
	 * @return Photo Metadaten mit Event UUID.
	 */
	public PhotoMetadaten eventZuordnung(PhotoMetadaten metadaten) {
		
		if (metadaten.getEventUuid() != null) {
			throw new IllegalArgumentException("Die Metadaten haben bereits eine Event UUID.");
		}
		
		// TODO es müssen nicht alle Events abgefragt werden, es reichen die, 
		// mit dem entsprechenden Alter wie in den Metadaten des Photos
		for (Event event : this.events.values()) {
			
			// Folgendes muss mit in das Query
			// Sind die Metadaten zu alt oder neu für das Event?
			if (!event.istZeitlichPassend(metadaten)) {
				continue;
			}
			
			// TODO vielleicht geht das mit mongodb direkt?
			final double distance = event.berechneDistanzZumMittelpunkt(metadaten);
			
			// Liegt der Ort im Umkreis des Events?
			double radius = event.getRadius();
			if (distance <= radius) {
				// Übernehme die Event UUID.
				metadaten.setEventUuid(event.getUuid());
				
				// Berechne den Mittelpunkt neu
				event.berechneNeuenMittelpunkt(metadaten);
				
				// Bei Bedarf erweitere den Radius um X
				// Photos die am Rand des Umkreises gemacht werden, 
				// sollen diesen vergrößern
				// Achtung: Hier wird getan, als seien es Meter
				event.erweitereUmkreis(distance);
				
				// Erweitere den zeitlichen Rahmen bei Bedarf
				event.erweitereZeitlichenRahmen(metadaten);
				
				this.speichereEvent(event);
				
				return metadaten;
			}
		}
		
		// Wenn nichts gefunden wird muss ein neues Event geschaffen werden
		Event event = new Event(metadaten);
		this.speichereEvent(event);
		
		return metadaten;
	}
}
