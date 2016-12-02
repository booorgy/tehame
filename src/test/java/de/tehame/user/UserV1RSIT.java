package de.tehame.user;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integrationstests müssen das Suffix 'IT' haben. 
 */
public class UserV1RSIT {

	/**
	 * Registriere einen zufälligen Benutzer und erwarte HTTP 200
	 * und eine UUID als Antwort.
	 * 
	 * @throws Exception I/O etc.
	 */
	@Test
	public void register() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/tehame/rest/v1/user");
		request.header("email", System.currentTimeMillis() + "@test.de");
		request.header("passwort", "%%%%%p4ssW0rt%%%%%");
		ClientResponse<String> response = request.post(String.class);
		Assert.assertEquals(200, response.getStatus());
		Assert.assertTrue(response.getEntity().matches(UserV1RSTest.UUID_REGEX));
	}
	
	@Test
	public void relations() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/tehame/rest/v1/user/relations");
		request.header("email", "admin_a@tehame.de");
		request.header("passwort", "a");
		ClientResponse<String> response = request.get(String.class);
		Assert.assertEquals(200, response.getStatus());
		// Admin A hat nur einen Freund: Admin B
		Assert.assertTrue(response.getEntity().equals("[\"e26fc393-9219-44b5-b681-f08f054a79eb\"]"));
	}
}
