package de.tehame.event;

import org.junit.Assert;
import org.junit.Test;

import de.tehame.event.Event;

public class DistanceTest {
	
	@Test
	public void distanzMessen() {
		Event e = new Event();
		
		// Etwas Abweichung ist OK
		double delta = 0.03d;
		
		// Vergleich mit Wert aus Google Maps
		// https://www.google.de/maps/dir/49.321863,2.688009/49.3494477,2.7004658/@49.3420872,2.6940267,13.47z
		Assert.assertEquals(3200d, e.haversine(49.321863, 2.688009, 49.3494477, 2.7004658), 3200d * delta);
		
		// Lat: Von Minus nach Plus
		// https://www.google.de/maps/dir/51.2672539,-0.0223269/51.2725844,0.0556324/@51.2753572,0.0021629,14z/data=!3m1!4b1
		Assert.assertEquals(5600d, e.haversine(51.2672539,-0.0223269, 51.2725844, 0.0556324), 5600d * delta);

		// Long: Von Minus nach Plus
		// https://www.google.de/maps/dir/-0.011022,29.7630183/0.0328065,29.7595163/@0.0109914,29.7675027,14.06z
		Assert.assertEquals(4900d, e.haversine(-0.011022, 29.7630183, 0.0328065, 29.7595163), 4900d * delta);
	}
}
