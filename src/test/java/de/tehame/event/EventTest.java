package de.tehame.event;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.photo.meta.PhotoMetadaten;

public class EventTest {
	
	@Test
	public void testGeo() {
		String[] labels = new String[0];
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		
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
		
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
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
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", -1L, 80, 80, 1920, 1080, "egal", "egal", 0, labels);
		p.bestimmeEventGeo(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt jedoch außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten("useruuid-xyz", -1L, -90, -90, 1920, 1080, "egal", "egal", 0, labels);
		p.bestimmeEventGeo(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
	}
	
	@Test
	public void testeErweiterungDesZeitlichenRahmens() {
		String[] labels = new String[0];
		// Das Ergebnis muss so aussehen, wenn die DIFFERENZ_SEKUNDEN 1 Stunde ist:
		// |----------------|----------------|
		// 0              3600             7200    Sekunden
		// ^start          ^metadaten        ^ende
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		Event e1 = new Event(m1);
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN - Event.DIFFERENZ_SEKUNDEN, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN + Event.DIFFERENZ_SEKUNDEN, e1.getEnds());
		
		// Erweitere den den zeitlichen Rahmen nochmals durch die gleichen Metadaten:
		// Das Photo ist bereits Teil des Events, somit sollte sich nichts ändern.
		e1.erweitereZeitlichenRahmen(m1);
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN - Event.DIFFERENZ_SEKUNDEN, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN + Event.DIFFERENZ_SEKUNDEN, e1.getEnds());
		
		// Dieses Photo sollte den Beginn des Events um 2 Sekunden nach *unten* verschieben, weil es 2 Sekunden früher gemacht wurde
		// Es darf als Startwert nicht -1 rauskommen, weil -1 als Marker für ungültige Werte verwendet wird
		// TODO Besseren Marker als -1 verwenden, z.B. integer.min_val
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN - 2, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		e1.erweitereZeitlichenRahmen(m2);
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN - Event.DIFFERENZ_SEKUNDEN - 2L, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN + Event.DIFFERENZ_SEKUNDEN, e1.getEnds());
		
		// Dieses Photo sollte den Beginn des Events um 2 Sekunden nach *oben* verschieben, weil es zwei Sekunden später gemacht wurde
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN + 2, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		e1.erweitereZeitlichenRahmen(m3);
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN - Event.DIFFERENZ_SEKUNDEN - 2L, e1.getBegins());
		Assert.assertEquals(Event.DIFFERENZ_SEKUNDEN + Event.DIFFERENZ_SEKUNDEN + 2L, e1.getEnds());
	}
	
	@Test
	public void testeEventsOhneBekanntenZeitlichenRahmen() {
		String[] labels = new String[0];
		
		// Events und Metadaten zu Fotos ohne bekannten Timestamp sind ein Sonderfall
		// Der Code -1 soll bei den Events erhalten bleiben
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		Event e1 = new Event(m1);
		Assert.assertEquals(-1L, e1.getBegins());
		Assert.assertEquals(-1L, e1.getEnds());
		
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", -1L, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		e1.erweitereZeitlichenRahmen(m2);
		Assert.assertEquals(-1L, e1.getBegins());
		Assert.assertEquals(-1L, e1.getEnds());
	}
	
	@Test
	public void testeRadiusErweiterung() {
		String[] labels = new String[0];
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 50.052100, 8.248486, 1920, 1080, "egal", "egal", 0, labels);
		Event e1 = new Event(m1);
		
		// Der Standardradius ist 100 Meter
		Assert.assertEquals(Event.RADIUS_INITIAL_METER, e1.getRadius(), 0d);
		
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", 0L, 50.0520346, 8.2520558, 1920, 1080, "egal", "egal", 0, labels);
		
		// https://www.google.de/maps/dir/50.0521,8.248486/50.0520346,8.2520558/@50.0520652,8.2492474,18z/data=!3m1!4b1!4m2!4m1!3e0
		// ca. 250 Meter
		double d1 = e1.berechneDistanzZumMittelpunkt(m2);
		Assert.assertEquals(250d, d1, 250d * 0.02); // 2% Abweichung sind OK

		// Normalerweise würden die Metadaten nicht mit dem Event gematcht werden, aber in diesem Test
		// wird das erzwungen, der neue Radius des Events muss ca. 260 sein
		// 250 > 100 => 250 + 10 = 260
		e1.erweitereUmkreis(d1);
		Assert.assertEquals(260d, e1.getRadius(), 260d * 0.02);
	}
	
	@Test
	public void testeZuordnungDerEventUUID() {
		String[] labels = new String[0];
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 0, 1920, 1080, "egal", "egal", 0, labels);
		Event e1 = new Event(m1);
		// Event und Metadaten müssen nun verknüpft sein
		Assert.assertEquals(m1.getEventUuid(), e1.getUuid());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testeUngueltigeZuordnung() {
		String[] labels = new String[0];
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", 0L, 0, 0, 1920, 1080, "egal", "egal", 0, labels);
		Event e1 = new Event(m1);
		// m1 darf nicht zwei Events zugeordnet sein
		Event e2 = new Event(m1);
	}
	
	@Test
	public void testGeoUndZeit() {
		String[] labels = new String[0];
		
		// 3600, 10, 10
		// 1h nach 1970
		PhotoMetadaten m1 = new PhotoMetadaten("useruuid-xyz", Event.DIFFERENZ_SEKUNDEN, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
		
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
		PhotoMetadaten m2 = new PhotoMetadaten("useruuid-xyz", 7200L, 10, 10, 1920, 1080, "egal", "egal", 0, labels);
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
		PhotoMetadaten m3 = new PhotoMetadaten("useruuid-xyz", 5000L, 80, 80, 1920, 1080, "egal", "egal", 0, labels);
		p.bestimmeEventGeoUndZeit(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(0L, p.sucheEvent(e3).getBegins());
		Assert.assertEquals(10800L, p.sucheEvent(e3).getEnds());
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt (nur) räumlich außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten("useruuid-xyz", 5000L, -90, -90, 1920, 1080, "egal", "egal", 0, labels);
		p.bestimmeEventGeoUndZeit(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
		Assert.assertEquals(5000L - Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e4).getBegins());
		Assert.assertEquals(5000L + Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e4).getEnds());
		
		// Das folgende Photo liegt (nur) zeitlich außerhalb: 10800+3600=14400
		PhotoMetadaten m5 = new PhotoMetadaten("useruuid-xyz", 14400L, 80, 80, 1920, 1080, "egal", "egal", 0, labels);
		p.bestimmeEventGeoUndZeit(m5);
		String e5 = m5.getEventUuid();
		
		Assert.assertNotEquals(e1, e5);
		Assert.assertNotEquals(e4, e5);
		Assert.assertEquals(14400L - Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e5).getBegins());
		Assert.assertEquals(14400L + Event.DIFFERENZ_SEKUNDEN, p.sucheEvent(e5).getEnds());
	}
}
