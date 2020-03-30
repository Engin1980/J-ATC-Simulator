package eng.jAtcSim.newLib.area.approaches.factories;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ThresholdInfo{
  public final Coordinate coordinate;
  public final int course;

  public ThresholdInfo(Coordinate coordinate, int course) {
    this.coordinate = coordinate;
    this.course = course;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getCourse() {
    return course;
  }
}
