/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.global;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marek Vajgl
 */
public class UnitProviderTest {
  
  public UnitProviderTest() {
  }

  @Test
  public void testKmToNM() {
//    double km = 1;
//    double expected = 0.
  }

  @Test
  public void testNmToKm() {
    double nm = 1;
    double expected = 1.852;
    double actual = UnitProvider.nmToKm(nm);
    assertEquals(expected, actual, 0.01);
  }

  @Test
  public void testFtToNm() {
  }

  @Test
  public void testNmToFt() {
  }
  
}
