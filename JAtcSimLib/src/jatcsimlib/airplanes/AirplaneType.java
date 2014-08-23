/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

/**
 *
 * @author Marek
 */
public class AirplaneType {

  public enum eType {

    prop,
    jet
  }

  public enum eSize {

    light,
    medium,
    heavy
  }

  /**
   * Name of type
   */
  public String name;
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

  public eType type;
  public eSize size;
}
