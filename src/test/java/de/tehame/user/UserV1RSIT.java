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
		ClientRequest request = new ClientRequest("http://localhost:8080/tehame/v1/user");
		request.header("email", System.currentTimeMillis() + "@test.de");
		request.header("passwort", "%%%%%p4ssW0rt%%%%%");
		ClientResponse<String> response = request.post(String.class);
		Assert.assertEquals(200, response.getStatus());
		Assert.assertTrue(response.getEntity().matches(UserV1RSTest.UUID_REGEX));
	}
}
