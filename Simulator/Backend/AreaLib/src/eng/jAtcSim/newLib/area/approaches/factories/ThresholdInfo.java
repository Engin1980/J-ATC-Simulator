package eng.jAtcSim.newLib.area.approaches.factories;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ThresholdInfo{
  public final Coordinate coordinate;
  public final int course;
  public final int altitude;
  public final String name;

  public ThresholdInfo(Coordinate coordinate, int course, int altitude, String name) {
    this.coordinate = coordinate;
    this.course = course;
    this.altitude = altitude;
    this.name = name;
  }
}
