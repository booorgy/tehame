package de.tehame.event;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import de.tehame.security.SecurableEndpoint;
import de.tehame.user.User;
import de.tehame.user.UserBean;

/**
 * JAX-RS Endpunkt für die Resource 'Event' in der API Version 1.
 * Die Annotation @Stateless macht diese Klasse auch zu einer zustandslosen EJB,
 * damit die 'UserBean' mit @EJB injected werden kann, um Transaktionen zu
 * ermöglichen.
 */
@Path("v1/event")
@Stateless
public class EventV1RS extends SecurableEndpoint {
	
	private static final Logger LOGGER = Logger.getLogger(EventV1RS.class);
	
	@EJB
	private UserBean userBean;
	
	@EJB
	private EventBean eventBean;
	
	/**
	 * Beispiel:
	 * curl -X GET http://localhost:8080/tehame/rest/v1/event -v -H "email: admin_a@tehame.de" -H "passwort: a"
	 * 
	 * @param email Aufrufer EMail.
	 * @param passwort Aufrufer Passwort.
	 * @return Alle Events, auf die der User Zugriff hat.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public List<String> alleEvents(
			@HeaderParam("email") String email,
			@HeaderParam("passwort") String passwort) {
		User user = this.userBean.sucheUser(email);
		this.auth(user, passwort, this.userBean);
		
		List<Event> events = this.eventBean.sucheEvents(user);
		List<String> eventUuids = new ArrayList<>(events.size());
		
		for (Event e : events) {
			eventUuids.add(e.getUuid());
		}
		
		LOGGER.trace("Der User " + user.getUuid() + " hat Zugriff auf die Events " + eventUuids.toString());
		
		return eventUuids;
	}
}
