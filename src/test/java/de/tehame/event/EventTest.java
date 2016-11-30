package de.tehame.event;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.photo.meta.PhotoMetadaten;

public class EventTest {

	@Test
	public void test() {
		PhotoMetadaten m1 = new PhotoMetadaten(-1L, 10, 10, 1920, 1080, "egal", "egal", 0);
		
		EventPlayground p = new EventPlayground();
		
		p.bestimmeEvent(m1);
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
		p.bestimmeEvent(m2);
		
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
		p.bestimmeEvent(m3);
		String e3 = m3.getEventUuid();
		
		Assert.assertEquals(e1, e3);
		
		// Dadurch, dass der Radius nun um 10 erweitert wurde, muss er bei ca. 108,995 liegen
		Assert.assertEquals(109d, p.sucheEvent(e3).getRadius(), 0.01d);
		
		// Das folgende Photo liegt jedoch außerhalb
		PhotoMetadaten m4 = new PhotoMetadaten(-1L, -90, -90, 1920, 1080, "egal", "egal", 0);
		p.bestimmeEvent(m4);
		String e4 = m4.getEventUuid();
		
		Assert.assertNotEquals(e1, e4);
	}
}
