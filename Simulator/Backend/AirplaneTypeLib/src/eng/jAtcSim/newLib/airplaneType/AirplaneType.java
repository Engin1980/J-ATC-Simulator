package eng.jAtcSim.newLib.airplaneType;

import eng.eSystem.exceptions.EApplicationException;
import exml.IXPersistable;
import exml.annotations.XAttribute;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.annotations.XOptional;
import exml.loading.XLoadContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneType implements IXPersistable {

  /**
   * Name of kind
   */
  @XAttribute public final String name = null;

  @XAttribute
  @XOptional
  public final String fullName = null;

  /**
   * Category (A, B, C or D)
   */
  @XAttribute public final char category = ' ';

  /**
   * Maximum altitude in ft
   */
  @XAttribute public final int maxAltitude = -1;

  /**
   * Rotation speed at take-off.
   */
  @XAttribute public final int vR = -1;

  /**
   * Minimum speed at approach
   */
  @XAttribute public final int vMinApp = -1;

  /**
   * Maximum speed at approach
   */
  @XAttribute public final int vMaxApp = -1;

  /**
   * Common speed at approach
   */
  @XAttribute public final int vApp = -1;

  /**
   * Minimum clean speed
   */
  @XAttribute public final int vMinClean = -1;

  /**
   * Maximum clean speed
   */
  @XAttribute public final int vMaxClean = -1;

  /**
   * Common cruise speed
   */
  @XAttribute public final int vCruise = -1;

  /**
   * Departure speed
   */
  @XAttribute public final int vDep = -1;

  @XIgnored public final int v2 = -1;

  /**
   * Climb rate ft/min at low altitudes
   */
  @XAttribute public final int lowClimbRate = -1;

  /**
   * Climb rate ft/min at high altitudes
   */
  @XAttribute public final int highClimbRate = -1;

  /**
   * Descend rate ft/min at high altitudes
   */
  @XAttribute public final int highDescendRate = -1;

  /**
   * Descend rate ft/min at low altitudes
   */
  @XAttribute public final int lowDescendRate = -1;

  /**
   * Rate of increase speed kts/sec
   */
  @XAttribute public final double speedIncreaseRate = -1;

  /**
   * Rate of decrease speed kts/sec
   */
  @XAttribute public final double speedDecreaseRate = -1;

  /**
   * Rate of heading change in degrees/second.
   */
  @XAttribute public final int headingChangeRate = -1;

  @XIgnored private final RateInfo _climb = null;
  @XIgnored private final RateInfo _descend = null;

  @XConstructor
  private AirplaneType(XLoadContext ctx) {
  }

//  public AirplaneType(String name, String fullName, char category, int maxAltitude, int vR,
//                      int vMinApp, int vMaxApp, int vApp, int vMinClean, int vMaxClean,
//                      int vCruise, int vDep, int lowClimbRate, int highClimbRate,
//                      int highDescendRate, int lowDescendRate, double speedIncreaseRate,
//                      double speedDecreaseRate, int headingChangeRate) {
//    this.name = name;
//    this.fullName = fullName;
//    this.category = category;
//    this.maxAltitude = maxAltitude;
//    this.vR = vR;
//    this.vMinApp = vMinApp;
//    this.vMaxApp = vMaxApp;
//    this.vApp = vApp;
//    this.vMinClean = vMinClean;
//    this.vMaxClean = vMaxClean;
//    this.vCruise = vCruise;
//    this.vDep = vDep;
//    this.lowClimbRate = lowClimbRate;
//    this.highClimbRate = highClimbRate;
//    this.highDescendRate = highDescendRate;
//    this.lowDescendRate = lowDescendRate;
//    this.speedIncreaseRate = speedIncreaseRate;
//    this.speedDecreaseRate = speedDecreaseRate;
//    this.headingChangeRate = headingChangeRate;
//    this.v2 = this.vR + 15;
//
//    {
//      double a = (this.lowClimbRate / 60d - this.highClimbRate / 60d) / (double) (0 - this.maxAltitude);
//      double b = this.highClimbRate / 60d - a * this.maxAltitude;
//      _climb = new RateInfo(a, b);
//    }
//    {
//      double a = (this.lowDescendRate / 60d - this.highDescendRate / 60d) / (double) (0 - this.maxAltitude);
//      double b = this.highDescendRate / 60d - a * this.maxAltitude;
//      _descend = new RateInfo(a, b);
//    }
//  }

  public double getClimbRateForAltitude(double altitude) {
    return _climb.a * altitude + _climb.b;
  }

  public double getDescendRateForAltitude(double altitude) {
    return _descend.a * altitude + _descend.b;
  }

  @Override
  public void postLoad(XLoadContext ctx) {
    setFinalField("v2", this.vR + 15);

    {
      double a = (this.lowClimbRate / 60d - this.highClimbRate / 60d) / (double) (0 - this.maxAltitude);
      double b = this.highClimbRate / 60d - a * this.maxAltitude;
      setFinalField("_climb", new RateInfo(a, b));
    }
    {
      double a = (this.lowDescendRate / 60d - this.highDescendRate / 60d) / (double) (0 - this.maxAltitude);
      double b = this.highDescendRate / 60d - a * this.maxAltitude;
      setFinalField("_descend", new RateInfo(a, b));
    }
  }

  @Override
  public String toString() {
    return String.format("%s {airplaneType}", this.name);
  }

  private void setFinalField(String name, Object value) {
    try {
      this.getClass().getField(name).set(this, value);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new EApplicationException(sf("Failed to set field '%s' internally.", name));
    }
  }
}
