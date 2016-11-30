package de.tehame.event;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.photo.meta.PhotoMetadaten;

public class EventTest {
	
	@Test
	public void testGeo() {
		PhotoMetadaten m1 = new PhotoMetadaten(-1L, 10, 10, 1920, 1080, "egal", "egal", 0);
		
		EventPlayground p = new EventPlayground();
		
		p.bestimmeEventGeo(m1);
		String e1 = m1.getEventUuid();
		Assert.assertNotNull(e1);
		Assert.assertNotNull(p.sucheEvent(e1));
		
		// Der Mittelpunkt sollte 10;10 sein
		Assert.assertEquals(10d, p.sucheEvent(e1).getLatitudeCenter(), 0d);
		Assert.assertEquals(10d, p.sucheEvent(e1).getLongitudeCenter(), 0d);
		
		String e2 = m1.getEventUuid();
		Assert.assertNotNull(p.sucheEvent(e2));
		
		Assert.assertEquals(e1, e2);
		
		PhotoMetadaten m2 = new PhotoMetadaten(-1L, 10, 10, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeo(m2);
		
		// Nun sollten es 2 Photos sein
		Assert.assertEquals(2, p.sucheEvent(e2).getAnzahlPhotos());
		
		// Der Mittelpunkt sollte immernoch bei 10;10 liegen
		Assert.assertEquals(10d, p.sucheEvent(e2).getLatitudeCenter(), 0d);
		Assert.assertEquals(10d, p.sucheEvent(e2).getLongitudeCenter(), 0d);
		
		// Der Radius sollte immernoch auf dem Startwert sein
		Assert.assertEquals(100d, p.sucheEvent(e2).getRadius(), 0d);
		
		// Auch dieses Photo liegt noch im Umkreis von 100 um den Mittelpunkt
		// sqrt(70*70*2) = 98,99494936611665
		PhotoMetadaten m3 = new PhotoMetadaten(-1L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeo(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt jedoch außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten(-1L, -90, -90, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeo(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
	}
	
	@Test
	public void testGeo2() {
		
	}
	
	@Test
	public void testGeoUndZeit() {
		// 3600, 10, 10
		// 1h nach 1970
		PhotoMetadaten m1 = new PhotoMetadaten(3600L, 10, 10, 1920, 1080, "egal", "egal", 0);
		
		EventPlayground p = new EventPlayground();
		
		p.bestimmeEventGeoUndZeit(m1);
		String e1 = m1.getEventUuid();
		Assert.assertNotNull(e1);
		Assert.assertNotNull(p.sucheEvent(e1));
		
		// Der Mittelpunkt sollte 10;10 sein
		// Der Timescope sollte von 0 bis 7200 gehen
		Assert.assertEquals(10d, p.sucheEvent(e1).getLatitudeCenter(), 0d);
		Assert.assertEquals(10d, p.sucheEvent(e1).getLongitudeCenter(), 0d);
		Assert.assertEquals(0L, p.sucheEvent(e1).getBegins());
		Assert.assertEquals(7200L, p.sucheEvent(e1).getEnds());
		
		String e2 = m1.getEventUuid();
		Assert.assertNotNull(p.sucheEvent(e2));
		
		Assert.assertEquals(e1, e2);
		
		// Dieses Photo sollte im Timescope liegen (0-7200)
		PhotoMetadaten m2 = new PhotoMetadaten(7200L, 10, 10, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m2);
		
		// Nun sollten es 2 Photos sein
		Assert.assertEquals(2, p.sucheEvent(e2).getAnzahlPhotos());
		
		// Der Mittelpunkt sollte immernoch bei 10;10 liegen
		Assert.assertEquals(10d, p.sucheEvent(e2).getLatitudeCenter(), 0d);
		Assert.assertEquals(10d, p.sucheEvent(e2).getLongitudeCenter(), 0d);
		
		// Der Radius sollte immernoch auf dem Startwert sein
		Assert.assertEquals(100d, p.sucheEvent(e2).getRadius(), 0d);
		
		// Das Ende muss auf 7200+3600=10800 erweitert sein
		Assert.assertEquals(0L, p.sucheEvent(e2).getBegins());
		Assert.assertEquals(10800L, p.sucheEvent(e2).getEnds());
		
		// Auch dieses Photo liegt noch im Umkreis von 100 um den Mittelpunkt
		// sqrt(70*70*2) = 98,99494936611665
		PhotoMetadaten m3 = new PhotoMetadaten(5000L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(0L, p.sucheEvent(e3).getBegins());
		Assert.assertEquals(10800L, p.sucheEvent(e3).getEnds());
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt (nur) räumlich außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten(5000L, -90, -90, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
		Assert.assertEquals(5000L - 3600L, p.sucheEvent(e4).getBegins());
		Assert.assertEquals(5000L + 3600L, p.sucheEvent(e4).getEnds());
		
		// Das folgende Photo liegt (nur) zeitlich außerhalb: 10800+3600=14400
		PhotoMetadaten m5 = new PhotoMetadaten(14400L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m5);
		String e5 = m5.getEventUuid();
		
		Assert.assertNotEquals(e1, e5);
		Assert.assertNotEquals(e4, e5);
		Assert.assertEquals(14400L - 3600L, p.sucheEvent(e5).getBegins());
		Assert.assertEquals(14400L + 3600L, p.sucheEvent(e5).getEnds());
	}
}
