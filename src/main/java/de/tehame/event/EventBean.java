package de.tehame.event;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jboss.logging.Logger;

import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.User;
import de.tehame.user.UserBean;

@Stateless
@LocalBean
public class EventBean {
	
	@PersistenceContext(unitName = "tehamePU")
	private EntityManager em;
	
	@Inject
	UserBean userBean;
	
	private static final Logger LOGGER = Logger.getLogger(EventBean.class);
	
	/**
	 * Sucht alle Events, die ein Benutzer sehen darf. Das sind die Events,
	 * bei denen der Benutzer selbst beteiligt ist und die anderer Benutzer,
	 * zu denen dieser eine Beziehung hat.
	 * 
	 * @param user User.
	 * @return Alle Events eines Users und dessen Relationen (1st Level).
	 */
	public List<Event> sucheEvents(List<String> uuids) { // TODO test
		
		// Muss absteigend sortiert sein, damit Photos korrekt zugeordnet werden,
		// d.h. dem neusten passenden Event zugeordnet werden, falls mehrere passen würden
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
		List<Event> events = this.sucheEvents(userBean.sucheRelationenMitZugehoerigkeit(user, metadaten.getZugehoerigkeit()));
		for (Event event : events) {
			
			// Folgendes muss mit in das Query
			// Sind die Metadaten zu alt oder neu für das Event?
			if (!event.istZeitlichPassend(metadaten)) {
				LOGGER.trace("Das Photo " + metadaten.getS3key() + " passt zeitlich nicht zu dem Event " + event.getUuid());
				continue;
			}
			
			// Distanz in Meter
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
				
				// Falls noch nicht vorhanden, ordne das Event dem User zu
				Predicate<User> userUuidVorhanden = u -> u.getUuid().equals(user.getUuid());
				
				// contains() kann man hier nicht verwenden, weil es auf equals() basiert
				if (!event.getUsers().stream().anyMatch(userUuidVorhanden)) {
					// Merge nur den User, das Event hängt dran
					this.verknuepfeUserUndEvent(user, event);
				} else {
					// Merge nur das Event, der User hängt schon dran
					this.em.merge(event);
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
