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
  private int windHeading;
  private int windSpeetInKts;
  private int visibilityInM;
  private int cloudBaseInFt;
  private double cloudBaseHitProbability;

  public static Weather createClear() {
    Weather ret = new Weather(
        210, 4, 9999, 13000, .2
    );
    return ret;
  }

  public Weather() {
  }

  public int getVisibilityInMeters() {
    return visibilityInM;
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

  public String toInfoString() {
    EStringBuilder sb = new EStringBuilder();
    if (Global.WEATHER_INFO_STRING_AS_METAR) {
      sb.append("METAR ");
      if (Acc.airport() == null)
        sb.append("???? ");
      else
        sb.append(Acc.airport().getIcao()).append(" ");
      sb.append("------Z ");
      sb.appendFormat("%03d%02dKT ", this.getWindHeading(), this.getWindSpeetInKts());
      sb.appendFormat("%04d ", this.getVisibilityInMeters());
      if (getCloudBaseHitProbability() == 0) {
        sb.append("NOSIG");
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
    } else
      sb.appendFormatLine("Wind %d° at %d kts, visibility %1.0f miles, cloud base at %d ft at %1.0f %%.",
          this.getWindHeading(),
          this.getWindSpeetInKts(),
          this.getVisibilityInMiles(),
          this.getCloudBaseInFt(),
          this.getCloudBaseHitProbability() * 100
      );
    return sb.toString();
  }
}
