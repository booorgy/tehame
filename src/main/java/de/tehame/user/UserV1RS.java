package de.tehame.user;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import de.tehame.TehameProperties;
import de.tehame.security.SecurableEndpoint;

/**
 * JAX-RS Endpunkt für die Resource 'User' in der API Version 1.
 * Die Annotation @Stateless macht diese Klasse auch zu einer zustandslosen EJB,
 * damit die 'UserBean' mit @EJB injected werden kann, um Transaktionen zu
 * ermöglichen.
 */
@Path("v1/user")
@Stateless
public class UserV1RS extends SecurableEndpoint {
	
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
		
		if (this.userBean.registrieren(user)) {
			LOGGER.debug("User mit UUID " + user.getUuid() + " registriert aber noch nicht verifiziert.");
			this.sendeEmailVerifizierung(user);
			return user.getUuid();
		} else {
			throw new WebApplicationException("User with email '" + email + "' already exists.", 
					Status.CONFLICT);
		}
	}
	
	/**
	 * Der Link zu dieser URI steht in der Regisrtierungs-EMail.
	 * Empfängt den Verifizierungsschlüssel zu einem User.
	 * Das Request muss GET sein, weil der User den Link anklicken will.
	 * 
	 * Beispiel:
	 * curl -X GET http://localhost:8080/tehame/rest/v1/user/e26fc393-9219-44b5-b681-f08f054a79eb/verify/d26fc39d-92d9-d4d5-d681-d08d054ad9ed
	 * 
	 * @param userUuid User UUID.
	 * @param secret Verifizierungsschlüssel.
	 * @return Webseite mit Bestätigung oder Ablehnung.
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Path("{useruuid}/verify/{secret}")
	public String verifiziere(@PathParam("useruuid") String userUuid, @PathParam("secret") String secret) {
		
		User user = this.userBean.sucheUserAnhandUuid(userUuid);
		
		if (user != null) {
			
			if (user.getVerifizierungsschluessel().equals(secret)) {
				
				user.setVerifiziert(true);
				user.setVerifizierungsschluessel(null);
				this.userBean.merge(user);
				
				return "<html><body><h1>Ihre Registrierung ist nun abgeschlossen</h1></body></html>";
			} else {
				return "<html><body><h1>Dieser Link ist abgelaufen</h1></body></html>"; // TODO Neue EMail anfordern
			}
		} else {
			return "<html><body><h1>Ungültiger Link</h1></body></html>";
		}
	}
	
	/**
	 * Beispiel:
	 * curl -X GET http://localhost:8080/tehame/rest/v1/user/relations -v -H "email: admin_a@tehame.de" -H "passwort: a"
	 * 
	 * @param email Aufrufer EMail.
	 * @param passwort Aufrufer Passwort.
	 * @return Alle User die mit dem Aufrufer in einer Relation stehen.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Path("relations")
	public List<String> sucheRelationen(
			@HeaderParam("email") String email,
			@HeaderParam("passwort") String passwort) {
		User user = this.userBean.sucheUser(email);
		this.auth(user, passwort, this.userBean);
		
		List<String> relationen = this.userBean.sucheRelationen(user);
		LOGGER.trace("Der User " + user.getUuid() + " hat Relationen zu " + relationen.toString());
		
		return relationen;
	}
	
	/**
	 * Sendet einen Link an den User via SES, den er klicken muss, um seine Registrierung abzuschließen.
	 * @param User An diesen User wird die Mail gesendet.
	 */
	private void sendeEmailVerifizierung(User user) {
		
		// TODO Diese EMail muss z.Z. eine in der AWS SES Sandbox verifizierte EMail sein.
        Destination destination = new Destination().withToAddresses(new String[] { user.getEmail() });
        Content subject = new Content().withData("Schließen Sie Ihre Registrierung jetzt ab");
        Content textBody = new Content().withData(
        		"Um Ihre Registrierung bei Tehame abzuschließen, klicken Sie bitte den folgenden"
        		+ " Link oder kopieren diesen in Ihre Browser-Adresszeile und öffnen Ihn dort."
        		+ " http://localhost:8080/tehame/rest/v1/user/" + user.getUuid() 
        		+ "/verify/" + user.getVerifizierungsschluessel()); // FIXME RPC Style
        Body body = new Body().withText(textBody);
        Message message = new Message().withSubject(subject).withBody(body);
        SendEmailRequest request = new SendEmailRequest().withSource(TehameProperties.SES_FROM).withDestination(destination).withMessage(message);
        
        try {        
            AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
            Region REGION = Region.getRegion(Regions.EU_WEST_1);
            client.setRegion(REGION);
       
            client.sendEmail(request);  
            LOGGER.trace("Registrierungs EMail an '" + user.getEmail() + "' gesendet.");
        } catch (Exception e) {
        	LOGGER.error("An '" + user.getEmail() + "' konnte nicht gesendet werden.", e);
        	// TODO grund analysieren, evtl registrierung abweisen
        }
	}
}
