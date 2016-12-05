package de.tehame.event;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.photo.meta.PhotoMetadaten;

public class EventTest {
	
	@Test
	public void testGeo() {
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0);
		
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
		
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0);
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
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", -1L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeo(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt jedoch außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten("useruuid-xyz", -1L, -90, -90, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeo(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
	}
	
	@Test
	public void testeErweiterungDesZeitlichenRahmens() {
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN, 10, 10, 1920, 1080, "egal", "egal", 0);
		Event e1 = new Event(m1);
		Assert.assertEquals(0L, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN * 2L, e1.getEnds());
		
		// Das Photo ist bereits Teil des Events, somit sollte sich nichts ändern 
		e1.erweitereZeitlichenRahmen(m1);
		
		Assert.assertEquals(0L, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN * 2L, e1.getEnds());
		
		// Dieses Photo sollte den Beginn des Events um 1 Sekunde nach unten verschieben
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN - 1, 10, 10, 1920, 1080, "egal", "egal", 0);
		e1.erweitereZeitlichenRahmen(m2);
		Assert.assertEquals(-1L, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN * 2L, e1.getEnds());
		
		// Dieses Photo sollte den Beginn des Events um 1 Sekunde nach oben verschieben
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN + 1, 10, 10, 1920, 1080, "egal", "egal", 0);
		e1.erweitereZeitlichenRahmen(m3);
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN * 2L + 1, e1.getEnds());
		Assert.assertEquals(-1L, e1.getBegins());
	}
	
	@Test
	public void testeRadiusErweiterung() {
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 0, 1920, 1080, "egal", "egal", 0);
		Event e1 = new Event(m1);
		
		// Der Standardradius ist 100
		Assert.assertEquals(100d, e1.getRadius(), 0d);
		
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 91, 1920, 1080, "egal", "egal", 0);
		double d1 = e1.berechneDistanzZumMittelpunkt(m2);
		Assert.assertEquals(91d, d1, 0d);

		// 91 + 10 = 101 > 100
		e1.erweitereUmkreis(d1);
		Assert.assertEquals(101d, e1.getRadius(), 0d);
	}
	
	@Test
	public void testeZuordnungDerEventUUID() {
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 0, 1920, 1080, "egal", "egal", 0);
		Event e1 = new Event(m1);
		// Event und Metadaten müssen nun verknüpft sein
		Assert.assertEquals(m1.getEventUuid(), e1.getUuid());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testeUngueltigeZuordnung() {
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 0, 1920, 1080, "egal", "egal", 0);
		Event e1 = new Event(m1);
		// m1 darf nicht zwei Events zugeordnet sein
		Event e2 = new Event(m1);
	}
	
	@Test
	public void testGeoUndZeit() {
		// 3600, 10, 10
		// 1h nach 1970
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN, 10, 10, 1920, 1080, "egal", "egal", 0);
		
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
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", 7200L, 10, 10, 1920, 1080, "egal", "egal", 0);
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
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", 5000L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(0L, p.sucheEvent(e3).getBegins());
		Assert.assertEquals(10800L, p.sucheEvent(e3).getEnds());
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt (nur) räumlich außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten("useruuid-xyz", 5000L, -90, -90, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
		Assert.assertEquals(5000L - Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e4).getBegins());
		Assert.assertEquals(5000L + Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e4).getEnds());
		
		// Das folgende Photo liegt (nur) zeitlich außerhalb: 10800+3600=14400
		PhotoMetadaten m5 = new PhotoMetadaten("useruuid-xyz", 14400L, 80, 80, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEventGeoUndZeit(m5);
		String e5 = m5.getEventUuid();
		
		Assert.assertNotEquals(e1, e5);
		Assert.assertNotEquals(e4, e5);
		Assert.assertEquals(14400L - Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e5).getBegins());
		Assert.assertEquals(14400L + Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e5).getEnds());
	}
}
