package de.tehame.event;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jboss.logging.Logger;

import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.Relation;
import de.tehame.user.User;

@Stateless
@LocalBean
public class EventBean {
	
	@PersistenceContext(unitName = "tehamePU")
	private EntityManager em;
	
	private static final Logger LOGGER = Logger.getLogger(EventBean.class);
	
	/**
	 * Sucht alle Events, die ein Benutzer sehen darf. Das sind die Events,
	 * bei denen der Benutzer selbst beteiligt ist und die anderer Benutzer,
	 * zu denen dieser eine Beziehung hat.
	 * 
	 * @param user User.
	 * @return Alle Events eines Users und dessen Relationen (1st Level).
	 */
	public List<Event> sucheEvents(User user, int zugehoerigkeit) { // TODO test
		User u = this.em.find(User.class, user.getUuid());
		List<Relation> relations = u.getRelations1();
		
		List<String> uuids = new LinkedList<String>();
		uuids.add(user.getUuid());
		
		// FIXME Ugly N+1 Problem
		for (Relation r : relations) {
			if (r.getId().getType() == zugehoerigkeit)
				uuids.add(r.getUser2().getUuid());
		}
		
		TypedQuery<Event> query = this.em.createQuery(
				"SELECT e FROM event AS e INNER JOIN e.users AS u WHERE u.uuid IN :useruuids ORDER BY e.ends DESC", 
				Event.class)
				.setParameter("useruuids", uuids);
		List<Event> events = query.getResultList();
		return events;
	}
	
	/**
	 * Die Metadaten werden einem existierenden oder neuen Event zugeordnet.
	 * 
	 * @param metadaten Photo Metadaten ohne Event UUID.
	 * @param user Der Besitzer des Photos.
	 * @return Photo Metadaten mit Event UUID.
	 */
	public PhotoMetadaten eventZuordnung(PhotoMetadaten metadaten, User user) {
		
		if (metadaten.getS3key() == null) {
			LOGGER.error("Die Metadaten haben keinen S3 Key.");
			throw new IllegalArgumentException("Die Metadaten haben keinen S3 Key.");
		}
		
		if (metadaten.getEventUuid() != null) {
			LOGGER.error("Die Metadaten haben bereits eine Event UUID.");
			throw new IllegalArgumentException("Die Metadaten haben bereits eine Event UUID.");
		}
		 
		// TODO es müssen nicht alle Events abgefragt werden, es reichen die, 
		// mit dem entsprechenden Alter wie in den Metadaten des Photos
		for (Event event : this.sucheEvents(user, metadaten.getZugehoerigkeit())) {
			
			// Folgendes muss mit in das Query
			// Sind die Metadaten zu alt oder neu für das Event?
			if (!event.istZeitlichPassend(metadaten)) {
				LOGGER.trace("Das Photo " + metadaten.getS3key() + " passt zeitlich nicht zu dem Event " + event.getUuid());
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
				
				LOGGER.trace("Das Photo " + metadaten.getS3key() + " wurde dem Event " + event.getUuid() + " zugeordnet");
				
				this.em.merge(event);
				
				// Falls noch nicht vorhanden, ordne das Event dem User zu
				if (!event.getUsers().contains(user)) {
					this.verknuepfeUserUndEvent(user, event);
				}
				
				return metadaten;
			}
		}
		
		// Wenn nichts gefunden wird muss ein neues Event geschaffen werden
		Event event = new Event(metadaten);
		LOGGER.trace("Für das Photo " + metadaten.getS3key() + " wurde das neue Event " + event.getUuid() + " geschaffen");
		this.em.persist(event);
		
		// Ordne das Event dem User zu
		this.verknuepfeUserUndEvent(user, event);
		
		return metadaten;
	}

	private void verknuepfeUserUndEvent(User user, Event event) {
		user = this.em.find(User.class, user.getUuid());
		user.getEvents().add(event);
		LOGGER.trace("Das Event " + event.getUuid() + " wird dem User " + user.getUuid() + " zugeordnet");
		this.em.merge(user);
	}
}
