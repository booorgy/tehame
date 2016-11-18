package de.tehame.user;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit-Tests müssen das Suffix 'Test' haben. 
 */
public class UserV1RSTest {
	
	public static final String UUID_REGEX = 
			"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
	@Test
	public void regexMussUuidMatchen() {
		Assert.assertTrue(
				UUID
				.randomUUID()
				.toString()
				.matches(UUID_REGEX));
	}
}
