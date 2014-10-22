/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.providers;

import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.CoordinateValue;
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
    double expResult = new CoordinateValue(282,04,46).get();
    double result = Coordinates.getBearing(a,b);
    //if (result.equals(expResult) == false)
    //  fail("Nevyšlo to. Expected: " + expResult.toString()+ ", actual: " + )
    assertEquals(expResult, result, 0.01);
  }
  
}
