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
  private int windHeading;
  private int windSpeetInKts;
  private int visibilityInM;
  private int cloudBaseInFt;
  public double cloudBaseHitProbability;

  public Weather() {
  }

  public Weather(int windHeading, int windSpeetInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability) {
    this.windHeading = windHeading;
    this.windSpeetInKts = windSpeetInKts;
    this.visibilityInM = visibilityInM;
    this.cloudBaseInFt = cloudBaseInFt;
    this.cloudBaseHitProbability = cloudBaseHitProbability;
  }

  /**
   * Returns visibility in nauctional miles
   * @return 
   */
  public double getVisibilityInMiles() {
    return UnitProvider.mToNM(visibilityInM);
  }

  /**
   * Returns lowest cloud altitude in ft above AGL
   * @return 
   */
  public int getCloudBaseInFt() {
    return cloudBaseInFt;
  }

  /**
   * Returns heading from which is incoming.
   * @return 
   */
  public int getWindHeading() {
    return windHeading;
  }

  /**
   * Returns speed of wind in kts.
   * @return 
   */
  public int getWindSpeetInKts() {
    return windSpeetInKts;
  }

  /**
   * Return 0..1 probability that pilot will not see through the clouds.
   * @return 0 if no clouds are covering ground view, 1 if clouds are covering ground view
   */
  public double getCloudBaseHitProbability() {
    return cloudBaseHitProbability;
  }
  
}
