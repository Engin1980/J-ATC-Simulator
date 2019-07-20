/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;


import eng.eSystem.geo.Coordinate;

/**
 * @author Marek
 */
public class Navaid {

  public enum eType {
    vor,
    ndb,
    fix,
    fixMinor,
    airport,
    auxiliary
  }
  public static final double SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER = 0.007;

  public static double getOverNavaidDistance(int speed) {
    return speed * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;
  }
  private Coordinate coordinate;
  private String name;
  private eType type;

  public Navaid(String name, eType type, Coordinate coordinate) {
    this.coordinate = coordinate;
    this.name = name;
    this.type = type;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + " {" + type + '}';
  }
}
