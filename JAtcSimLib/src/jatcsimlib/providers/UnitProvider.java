/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.providers;

/**
 *
 * @author Marek
 */
public class UnitProvider {
  public static double kmToNM(double value){
    return value * 0.539968;
  }

  static double nmToKm(double value) {
    return value / 0.539968;
  }
}
