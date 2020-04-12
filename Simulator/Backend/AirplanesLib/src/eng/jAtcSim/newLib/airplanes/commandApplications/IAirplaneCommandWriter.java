package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneCommandWriter {
  void abortHolding();

  void clearedToApproach(NewApproachInfo nai);

  void setAltitudeRestriction(Restriction restriction);

  void setSpeedRestriction(Restriction restriction);

  void setTargetAltitude(int altitudeInFt);

  void setTargetHeading(double targetHeading, LeftRight turn);

  void takeOff(String runwayThresholdName);
}
