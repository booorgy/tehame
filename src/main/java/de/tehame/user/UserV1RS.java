package de.tehame.user;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

/**
 * JAX-RS Endpunkt für die Resource 'User' in der API Version 1.
 * Die Annotation @Stateless macht diese Klasse auch zu einer zustandslosen EJB,
 * damit die 'UserBean' mit @EJB injected werden kann, um Transaktionen zu
 * ermöglichen.
 */
@Path("v1/user")
@Stateless
public class UserV1RS {
	
	private static final Logger LOGGER = Logger.getLogger(UserV1RS.class);
	
	@EJB
	private UserBean userBean;
	
	/**
	 * Registriert einen neuen Benutzer.
	 * Beispiel: curl -X POST http://localhost:8080/tehame/rest/v1/user -H "email: gude@gude.de" -H "passwort: a" -v
	 * Diese Methode wird nicht als Teil einer Transaktion ausgeführt, 
	 * die Methoden der UserBean hingegen schon.
	 * 
	 * @param email HTTP-Header 'email'.
	 * @param passwort HTTP-Header 'passwort'.
	 * @return UUID des neuen Users.
	 */
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public String register(@HeaderParam("email") String email, @HeaderParam("passwort") String passwort) {
		User user = new User(email, passwort);
		
		// TODO Validierung EMail Syntax, Passwort Constraints
		// TODO Mail senden zur Bestätigung
		
		if (this.userBean.registrieren(user)) {
			LOGGER.debug("User mit UUID " + user.getUuid() + " registriert.");
			return user.getUuid();
		} else {
			throw new WebApplicationException("User with email '" + email + "' already exists.", 
					Status.CONFLICT);
		}
	}
}
