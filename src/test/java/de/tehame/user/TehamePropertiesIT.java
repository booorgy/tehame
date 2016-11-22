package de.tehame.user;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.TehameProperties;

public class TehamePropertiesIT {

	@Test
	public void umgebungsvariablenMuessenGesetztSein() {
		Assert.assertNotNull("Mongo DB URL muss gesetzt sein", TehameProperties.MONGO_DB_URL);
	}
}
