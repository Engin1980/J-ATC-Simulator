package eng.jAtcSim.newLib.area;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.RegexGrouper;

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

  public static Navaid create(String name, eType type, Coordinate coordinate) {
    if (name == null) {
      throw new IllegalArgumentException("Value of {name} cannot not be null.");
    }
    if (coordinate == null) {
      throw new IllegalArgumentException("Value of {coordinate} cannot not be null.");
    }

    Navaid ret = new Navaid();
    ret.coordinate = coordinate;
    ret.name = name;
    ret.type = type;
    return ret;
  }

  public static double getOverNavaidDistance(int speed) {
    return speed * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;
  }

  private Coordinate coordinate;
  private String name;
  private eType type;

  private Navaid() {
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
