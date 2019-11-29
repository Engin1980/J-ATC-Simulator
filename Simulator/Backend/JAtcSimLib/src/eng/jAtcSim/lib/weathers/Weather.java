/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.weathers;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.global.UnitProvider;

/**
 * @author Marek
 */
public class Weather {

  public enum eSnowState{
    none,
    normal,
    intensive
  }

  public static final double WIND_GUST_PROBABILITY = 0.1;
  private int windHeading;
  private int windSpeetInKts;
  private int windGustSpeedInKts;
  private int visibilityInM;
  private int cloudBaseInFt;
  private eSnowState snowState;
  private double cloudBaseHitProbability;

  public static Weather createClear() {
    Weather ret = new Weather(
        210, 4, 4, 9999, 13000, .2, eSnowState.none
    );
    return ret;
  }

  public Weather() {
  }

  public int getVisibilityInMeters() {
    return visibilityInM;
  }

  public Weather(int windHeading, int windSpeetInKts, int windGustSpeedInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability, eSnowState snowState) {
    this.windHeading = windHeading;
    this.windSpeetInKts = windSpeetInKts;
    this.windGustSpeedInKts = windGustSpeedInKts;
    this.visibilityInM = visibilityInM;
    this.cloudBaseInFt = cloudBaseInFt;
    this.cloudBaseHitProbability = cloudBaseHitProbability;
    this.snowState = snowState;
  }

  /**
   * Returns visibility in nauctional miles
   *
   * @return
   */
  public double getVisibilityInMiles() {
    return UnitProvider.mToNM(visibilityInM);
  }

  public double getVisibilityInMilesReal() {
    if (visibilityInM == 9999)
      return Double.MAX_VALUE;
    else
      return getVisibilityInMiles();
  }

  public int getWindSpeedOrWindGustSpeed(){
    if (Acc.rnd().nextDouble() < WIND_GUST_PROBABILITY)
      return windGustSpeedInKts;
    else
      return windSpeetInKts;
  }

  /**
   * Returns lowest cloud altitude in ft above AGL
   *
   * @return
   */
  public int getCloudBaseInFt() {
    return cloudBaseInFt;
  }

  /**
   * Returns heading from which is incoming.
   *
   * @return
   */
  public int getWindHeading() {
    return windHeading;
  }

  /**
   * Returns speed of wind in kts.
   *
   * @return
   */
  public int getWindSpeetInKts() {
    return windSpeetInKts;
  }

  /**
   * Return 0..1 probability that pilot will not see through the clouds.
   *
   * @return 0 if no clouds are covering ground view, 1 if clouds are covering ground view
   */
  public double getCloudBaseHitProbability() {
    return cloudBaseHitProbability;
  }

  public int getWindGustSpeedInKts() {
    return windGustSpeedInKts;
  }

  public String toInfoString() {
    EStringBuilder sb = new EStringBuilder();
    if (Global.WEATHER_INFO_STRING_AS_METAR) {
      sb.append("METAR ");
      if (Acc.airport() == null)
        sb.append("???? ");
      else
        sb.append(Acc.airport().getIcao()).append(" ");
      sb.appendFormat("%02d", java.time.LocalDate.now().getDayOfMonth());
      sb.appendFormat("%02d%02dZ ", Acc.now().getHours(), Acc.now().getMinutes());
      if (this.getWindSpeetInKts() == this.getWindGustSpeedInKts())
        sb.appendFormat("%03d%02dKT ", this.getWindHeading(), this.getWindSpeetInKts());
      else
        sb.appendFormat("%03d%02G%02KT ", this.getWindHeading(), this.getWindSpeetInKts(), this.getWindGustSpeedInKts());
      sb.appendFormat("%04d ", this.getVisibilityInMeters());
      if (snowState == eSnowState.normal)
        sb.append("SN " );
      else if (snowState == eSnowState.intensive)
        sb.append("+SN ");
      if (getCloudBaseHitProbability() == 0) {
        sb.append("SKC");
      } else {
        if (getCloudBaseHitProbability() < 2d / 8)
          sb.append("FEW");
        else if (getCloudBaseHitProbability() < 5d / 8)
          sb.append("BKN");
        else if (getCloudBaseHitProbability() < 7d / 8)
          sb.append("SCT");
        else
          sb.append("OVC");
        sb.appendFormat("%03d", this.getCloudBaseInFt() / 100);
      }
      sb.append(" ...");
    } else {
      sb.appendFormatLine("Wind %d° at %d (%d) kts, visibility %1.0f miles, cloud base at %d ft at %1.0f %%.",
          this.getWindHeading(),
          this.getWindSpeetInKts(),
          this.getWindGustSpeedInKts(),
          this.getVisibilityInMiles(),
          this.getCloudBaseInFt(),
          this.getCloudBaseHitProbability() * 100
      );
      if (snowState == eSnowState.normal)
        sb.append(" Snowing.");
      else if (snowState == eSnowState.intensive)
        sb.append(" Intensive snowing.");
    }
    return sb.toString();
  }

  public eSnowState getSnowState() {
    return this.snowState;
  }
}
