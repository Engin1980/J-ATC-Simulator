package eng.jAtcSim.abstractRadar;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.area.Atc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneInfo {
  int altitude();

  Callsign callsign();

  Coordinate coordinate();

  AirproxType getAirprox();

  boolean hasRadarContact();

  int heading();

  int ias();

  boolean isEmergency();

  boolean isMrvaError();

  boolean isUnderConfirmedSwitch();

  AirplaneType planeType();

  AtcId responsibleAtc();

  Squawk squawk();

  int targetAltitude();

  int targetHeading();

  double targetSpeed();

  double tas();

  AtcId tunedAtc();

  int verticalSpeed();
}
