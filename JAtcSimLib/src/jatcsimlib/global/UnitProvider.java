/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

/**
 *
 * @author Marek
 */
public class UnitProvider {
  public static double kmToNM(double value){
    return value * 0.539968;
  }

  public static double nmToKm(double value) {
    return value / 0.539968;
  }
  
  public static double ftToNm(double value){
    return value / 6077.1;
  }
  public static double nmToFt (double value){
    return value * 6076.1;
  }
}
