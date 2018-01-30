/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.providers;

import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.CoordinateValue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marek
 */
public class CoordinateProviderTest {

  @Test
  public void testGetDistanceInNM() {
    Coordinate a = new Coordinate(50, 06, 03, 14, 15, 36);
    Coordinate b = new Coordinate(51, 8.88, 0, -11.42);
    double expResult = 553.07926;
    double result = Coordinates.getDistanceInNM(a, b);
    assertEquals(expResult, result, 0.1);
  }

  @Test
  public void testGetBearing() {
    Coordinate a = new Coordinate(50, 06, 03, 14, 15, 36);
    Coordinate b = new Coordinate(51, 8, 52.8, 0, -11, 25.2);
    double expResult = new CoordinateValue(282, 04, 46).get();
    double result = Coordinates.getBearing(a, b);
    assertEquals(expResult, result, 0.01);
  }

  @Test
  public void testGetCoordinate() {
    Coordinate point = new Coordinate(50,14);
    int bearing = 87;
    double distanceInNM = 0.094722222;

    Coordinate result = Coordinates.getCoordinate(point, bearing, distanceInNM);
    Coordinate expResult = new Coordinate(50, 0, 0, 14, 0, 8);

    assertEquals(expResult.getLatitude().get(), result.getLatitude().get(), 0.0001);
    assertEquals(expResult.getLongitude().get(), result.getLongitude().get(), 0.001);
  }
  
  @Test
  public void testGetCoordinate2() {
    Coordinate point = new Coordinate(50.04325, 14.39449);
    int bearing = 304;
    double distanceInNM = 145.6d/3600d;

    Coordinate result = Coordinates.getCoordinate(point, bearing, distanceInNM);
    Coordinate expResult = new Coordinate(50.0436266802, 14.3936204071);

    assertEquals(expResult.getLatitude().get(), result.getLatitude().get(), 0.0001);
    assertEquals(expResult.getLongitude().get(), result.getLongitude().get(), 0.0001);
  }
  
  @Test
  public void testGetCoordinate3() {
    Coordinate point = new Coordinate(49.90889, 13.24380);
    int bearing = 92;
    double distanceInNM = 185.68575;

    Coordinate result = Coordinates.getCoordinate(point, bearing, distanceInNM);
    Coordinate expResult = new Coordinate(49.7023236196, 18.0259258668);

    assertEquals(expResult.getLatitude().get(), result.getLatitude().get(), 0.0001);
    assertEquals(expResult.getLongitude().get(), result.getLongitude().get(), 0.001);
  }
  
  @Test
  public void testGetCoordinate4() {
    Coordinate point = new Coordinate(50, 10);
    int bearing = 100;
    double distanceInNM = 53.99568; // 100/3600;

    Coordinate result = Coordinates.getCoordinate(point, bearing, distanceInNM);
    Coordinate expResult = new Coordinate(49, 50, 8, 11, 22, 24);

    assertEquals(expResult.getLatitude().get(), result.getLatitude().get(), 0.001);
    assertEquals(expResult.getLongitude().get(), result.getLongitude().get(), 0.001);
  }

}
