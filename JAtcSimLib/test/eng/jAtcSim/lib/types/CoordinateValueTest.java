/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.types;

import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.coordinates.CoordinateValue;
import eng.jAtcSim.lib.global.Global;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marek
 */
public class CoordinateValueTest {
  
  public CoordinateValueTest() {
  }

  @Test
  public void testSet_double() {
    double value = 79.505;
    CoordinateValue instance = new CoordinateValue(value);
    
    double exp = 79.505;
    assertEquals(exp, value, 0);
  }

  @Test
  public void testSet_3args() {
    int degrees = 79;
    int minutes = 54;
    double seconds = 23.32;
    CoordinateValue instance = new CoordinateValue(degrees, minutes, seconds);
    
    int actDegrees = instance.getDegrees();
    int actMinutes = instance.getMinutes();
    double actSeconds = instance.getSeconds();
    
    assertEquals(degrees, actDegrees);
    assertEquals(minutes, actMinutes);
    assertEquals(seconds, actSeconds, 0.0000001);
  }
  
  @Test
  public void testSet_3args_2() {
    int degrees = 0;
    int minutes = 11;
    double seconds = 14.32;
    CoordinateValue instance = new CoordinateValue(degrees, minutes, seconds);
    
    int actDegrees = instance.getDegrees();
    int actMinutes = instance.getMinutes();
    double actSeconds = instance.getSeconds();
    
    assertEquals(degrees, actDegrees);
    assertEquals(minutes, actMinutes);
    assertEquals(seconds, actSeconds, 0.0000001);
  }
  
  @Test
  public void testSet_2args() {
    int degrees = 0;
    double minutes = 11.25;
    CoordinateValue instance = new CoordinateValue(degrees, minutes);
    
    double actValue = instance.get();
    
    assertEquals(degrees + minutes/60d, actValue, 0.000001);
  }

  @Test
  public void testGetDegrees() {
    CoordinateValue instance = new CoordinateValue(55.5);
    int expResult = 55;
    int result = instance.getDegrees();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetTotalDegrees() {
    CoordinateValue instance = new CoordinateValue(55.5);
    double expResult = 55.5;
    double result = instance.getTotalDegrees();
    assertEquals(expResult, result, 0.0);
  }

  @Test
  public void testGetMinutes() {
    CoordinateValue instance = new CoordinateValue(55.5);
    int expResult = 30;
    int result = instance.getMinutes();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetSeconds() {
    CoordinateValue instance = new CoordinateValue(0);
    double expResult = 0.0;
    double result = instance.getSeconds();
    assertEquals(expResult, result, 0.0);
  }

  @Test
  public void testClone() {
    CoordinateValue instance = new CoordinateValue(24.232);
    CoordinateValue expResult = new CoordinateValue(24.232);
    CoordinateValue result = instance.clone();
    
    if (result == expResult)
      fail("Instances should not be \"==\".");
    if (result.equals(expResult) == false){
      fail("Instances should be \"equal\".");
    }
    if (result.isSame(expResult) == false){
      fail("Instances should be \"same\".");
    }
  }

  @Test
  public void testToString() {
    CoordinateValue instance = new CoordinateValue(30, 12, 12.23);
    String expResult = "30°12'12,23\"";
    if (Global.COORDINATE_LONG == false){
      expResult = "30,20340";
    }
    String result = instance.toString();
    assertEquals(expResult, result);
  }
  
}
