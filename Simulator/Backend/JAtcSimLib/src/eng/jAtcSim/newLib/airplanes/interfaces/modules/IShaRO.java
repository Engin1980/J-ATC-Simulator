package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.global.Restriction;

public interface IShaRO {
  int getAltitude();

  int getGS();

  int getTAS();

  int getTargetAltitude();

  int getTargetSpeed();

  int getVerticalSpeed();

  Coordinate tryGetTargetCoordinate();

  Restriction getSpeedRestriction();

  int getSpeed();

  int getTargetHeading();

  int getHeading();
}
