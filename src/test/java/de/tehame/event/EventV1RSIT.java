package de.tehame.event;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integrationstests müssen das Suffix 'IT' haben. 
 */
public class EventV1RSIT {
	
	@Test
	public void events() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/tehame/rest/v1/event");
		request.header("email", "admin_a@tehame.de");
		request.header("passwort", "a");
		ClientResponse<String> response = request.get(String.class);
		Assert.assertEquals(200, response.getStatus());
		// Admin A sieht sein eigenes Event und das eine Event seines Freundes Admin B
		Assert.assertTrue(response.getEntity().equals(
				"[\"e26fc393-9219-44b5-b681-f08f054a79ex\",\"e26fc393-9219-44b5-b681-f08f054a79ey\"]"));
	}
}
