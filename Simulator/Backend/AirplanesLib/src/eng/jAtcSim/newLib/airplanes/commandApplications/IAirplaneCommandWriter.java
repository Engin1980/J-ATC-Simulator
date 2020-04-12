package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneCommandWriter {
  void abortHolding();

  void clearedToApproach(NewApproachInfo nai);

  void sendMessage(ISpeech s);

  void setAltitudeRestriction(Restriction restriction);

  void setSpeedRestriction(Restriction restriction);

  void setTargetAltitude(int altitudeInFt);

  void setTargetHeading(double targetHeading, LeftRight turn);

  void takeOff(String runwayThresholdName);
}
