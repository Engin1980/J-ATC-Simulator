/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

/**
 *
 * @author Marek
 */
public class AirplaneType {

  /**
   * Name of type
   */
  public String name;
  
  /**
   * Category (A, B, C or D)
   */
  public char category;
  
  /**
   * Maximum altitude in ft
   */
  public int maxAltitude;
  
  /**
   * Rotation speed at take-off.
   */
  public int vR;
  
  /**
   * Minimum speed at approach
   */
  public int vMinApp;
  
  /**
   * Maximum speed at approach
   */
  public int vMaxApp;
  
  /**
   * Common speed at approach
   */
  public int vApp;
  
  /**
   * Minimum clean speed
   */
  public int vMinClean;
  
  /**
   * Maximum clean speed
   */
  public int vMaxClean;
  
  /**
   * Common cruise speed
   */
  public int vCruise;
  
  /**
   * Departure speed
   */
  public int vDep;

  /**
   * Climb rate ft/min at low altitudes
   */
  public int lowClimbRate;

  /**
   * Climb rate ft/min at high altitudes
   */
  public int highClimbRate;

  /**
   * Descend rate ft/min at high altitudes
   */
  public int highDescendRate;

  /**
   * Descend rate ft/min at low altitudes
   */
  public int lowDescendRate;

  /**
   * Rate of increase speed kts/sec
   */
  public int speedIncreaseRate;

  /**
   * Rate of decrease speed kts/sec
   */
  public int speedDecreaseRate;

  /**
   * Rate of heading change in degrees/second.
   */
  public int headingChangeRate;

  private RateInfo _climb = null;
  private RateInfo _descend = null;

  public double getClimbRateForAltitude(int altitude) {
    if (_climb == null) {
      double a = (this.lowClimbRate/60d - this.highClimbRate/60d) / (double) (0 - this.maxAltitude);
      double b = this.highClimbRate/60d - a * this.maxAltitude;
      _climb = new RateInfo(a, b);
    }

    return _climb.a * altitude + _climb.b;
  }

  public double getDescendRateForAltitude(int altitude) {
    if (_descend == null) {
      double a = (this.lowDescendRate/60d - this.highDescendRate/60d) / (double) (0 - this.maxAltitude);
      double b = this.highDescendRate/60d - a * this.maxAltitude;
      _descend = new RateInfo(a, b);
    }

    return _descend.a * altitude + _descend.b;
  }

}

class RateInfo {

  public final double a;
  public final double b;

  public RateInfo(double a, double b) {
    this.a = a;
    this.b = b;
  }

}
