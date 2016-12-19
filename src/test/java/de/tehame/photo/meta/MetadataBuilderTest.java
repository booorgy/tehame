package de.tehame.photo.meta;

import org.junit.Assert;
import org.junit.Test;

public class MetadataBuilderTest {

	@Test
	public void aufnahmeDatumConverter() {
		// Beispiel zu Exif Original Date and Time: 2003:08:11 16:45:32
		//                                          YYYY:MM:DD HH:mm:ss
		
		// 1060620332 == 2003-08-11T16:45:32+00:00 in ISO 8601 (UTC)
		Assert.assertEquals(1060620332L, MetadataBuilder.toUnixTimestamp("2003:08:11 16:45:32"));
		
		// 1050620332 = 2003-04-17T22:58:52+00:00 in ISO 8601
		Assert.assertEquals(1050620332L, MetadataBuilder.toUnixTimestamp("2003:04:17 22:58:52"));
		
		// 50620332 = 1971-08-09T21:12:12+00:00 in ISO 8601
		Assert.assertEquals(50620332L, MetadataBuilder.toUnixTimestamp("1971:08:09 21:12:12"));
		
		// 2250620332 = 2041-04-26T20:18:52+00:00 in ISO 8601
		Assert.assertEquals(2250620332L, MetadataBuilder.toUnixTimestamp("2041:04:26 20:18:52"));
		
		// Fehler sollen in -1 resultieren
		Assert.assertEquals(-1L, MetadataBuilder.toUnixTimestamp("X"));
	}
}
