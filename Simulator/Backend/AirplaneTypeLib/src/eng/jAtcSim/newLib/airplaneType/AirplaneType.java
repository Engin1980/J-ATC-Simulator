package eng.jAtcSim.newLib.airplaneType;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class AirplaneType {
  public static AirplaneType load(XElement source) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    String fullName = XmlLoader.loadString("fullName");
    char category = XmlLoader.loadStringRestricted("category", new String[]{"A", "B", "C", "D"}).charAt(0);
    int maxAltitude = XmlLoader.loadInteger("maxAltitude");
    int vMinApp = XmlLoader.loadInteger("vMinApp");
    int vApp = XmlLoader.loadInteger("vApp");
    int vMaxApp = XmlLoader.loadInteger("vMaxApp");
    int vR = XmlLoader.loadInteger("vR");
    int vDep = XmlLoader.loadInteger("vDep");
    int vMinClean = XmlLoader.loadInteger("vMinClean");
    int vMaxClean = XmlLoader.loadInteger("vMaxClean");
    int vCruise = XmlLoader.loadInteger("vCruise");
    int lowClimbRate = XmlLoader.loadInteger("lowClimbRate");
    int highClimbRate = XmlLoader.loadInteger("highClimbRate");
    int lowDescendRate = XmlLoader.loadInteger("lowDescendRate");
    int highDescendRate = XmlLoader.loadInteger("highDescendRate");
    int speedIncreaseRate = XmlLoader.loadInteger("speedIncreaseRate");
    int speedDecreaseRate = XmlLoader.loadInteger("speedDecreaseRate");
    int headingChangeRate = XmlLoader.loadInteger("headingChangeRate");

    AirplaneType ret = new AirplaneType(name, fullName, category,maxAltitude,
        vR, vMinApp, vMaxApp, vApp,
        vMinClean, vMaxClean, vCruise, vDep,
        lowClimbRate, highClimbRate, highDescendRate, lowDescendRate,
        speedIncreaseRate, speedDecreaseRate, headingChangeRate);
    return ret;
  }

  /**
   * Name of kind
   */
  public final String name;

  public final String fullName;

  /**
   * Category (A, B, C or D)
   */
  public final char category;

  /**
   * Maximum altitude in ft
   */
  public final int maxAltitude;

  /**
   * Rotation speed at take-off.
   */
  public final int vR;

  /**
   * Minimum speed at approach
   */
  public final int vMinApp;

  /**
   * Maximum speed at approach
   */
  public final int vMaxApp;

  /**
   * Common speed at approach
   */
  public final int vApp;

  /**
   * Minimum clean speed
   */
  public final int vMinClean;

  /**
   * Maximum clean speed
   */
  public final int vMaxClean;

  /**
   * Common cruise speed
   */
  public final int vCruise;

  /**
   * Departure speed
   */
  public final int vDep;

  public final int v2;

  /**
   * Climb rate ft/min at low altitudes
   */
  public final int lowClimbRate;

  /**
   * Climb rate ft/min at high altitudes
   */
  public final int highClimbRate;

  /**
   * Descend rate ft/min at high altitudes
   */
  public final int highDescendRate;

  /**
   * Descend rate ft/min at low altitudes
   */
  public final int lowDescendRate;

  /**
   * Rate of increase speed kts/sec
   */
  public final double speedIncreaseRate;

  /**
   * Rate of decrease speed kts/sec
   */
  public final double speedDecreaseRate;

  /**
   * Rate of heading change in degrees/second.
   */
  public final int headingChangeRate;

  private final RateInfo _climb;
  private final RateInfo _descend;

  public AirplaneType(String name, String fullName, char category, int maxAltitude, int vR,
                      int vMinApp, int vMaxApp, int vApp, int vMinClean, int vMaxClean,
                      int vCruise, int vDep, int lowClimbRate, int highClimbRate,
                      int highDescendRate, int lowDescendRate, double speedIncreaseRate,
                      double speedDecreaseRate, int headingChangeRate) {
    this.name = name;
    this.fullName = fullName;
    this.category = category;
    this.maxAltitude = maxAltitude;
    this.vR = vR;
    this.vMinApp = vMinApp;
    this.vMaxApp = vMaxApp;
    this.vApp = vApp;
    this.vMinClean = vMinClean;
    this.vMaxClean = vMaxClean;
    this.vCruise = vCruise;
    this.vDep = vDep;
    this.lowClimbRate = lowClimbRate;
    this.highClimbRate = highClimbRate;
    this.highDescendRate = highDescendRate;
    this.lowDescendRate = lowDescendRate;
    this.speedIncreaseRate = speedIncreaseRate;
    this.speedDecreaseRate = speedDecreaseRate;
    this.headingChangeRate = headingChangeRate;
    this.v2 = this.vR + 15;

    {
      double a = (this.lowClimbRate / 60d - this.highClimbRate / 60d) / (double) (0 - this.maxAltitude);
      double b = this.highClimbRate / 60d - a * this.maxAltitude;
      _climb = new RateInfo(a, b);
    }
    {
      double a = (this.lowDescendRate / 60d - this.highDescendRate / 60d) / (double) (0 - this.maxAltitude);
      double b = this.highDescendRate / 60d - a * this.maxAltitude;
      _descend = new RateInfo(a, b);
    }
  }

  public double getClimbRateForAltitude(double altitude) {
    return _climb.a * altitude + _climb.b;
  }

  public double getDescendRateForAltitude(double altitude) {
    return _descend.a * altitude + _descend.b;
  }

  @Override
  public String toString() {
    return String.format("%s {airplaneType}", this.name);
  }
}
