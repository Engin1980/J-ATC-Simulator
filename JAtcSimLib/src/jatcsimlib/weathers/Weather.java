/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.weathers;

import jatcsimlib.global.UnitProvider;

/**
 *
 * @author Marek
 */
public class Weather {
  private int visibilityInMeters;
  private int lowCloudAltitudeInFt;

  public Weather() {
  }

  public double getVisibilityInMiles() {
    return UnitProvider.kmToNM(visibilityInMeters);
  }

  public int getLowCloudAltitudeInFt() {
    return lowCloudAltitudeInFt;
  }
  
  
}
