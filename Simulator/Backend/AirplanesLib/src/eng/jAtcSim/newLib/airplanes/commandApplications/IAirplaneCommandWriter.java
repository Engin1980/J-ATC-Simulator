package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.airplanes.pilots.updaters.IHeadingUpdater;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneCommandWriter {
  void abortHolding();

  void clearedToApproach(NewApproachInfo nai);

  void divert(boolean unknown);

  void goAround(GoingAroundNotification.GoAroundReason reason);

  void hold(Navaid navaid, int inboundRadial, LeftRight turn);

  void sendMessage(ISpeech s);

  void setAltitudeRestriction(Restriction restriction);

  void setSpeedRestriction(Restriction restriction);

  void setTargetAltitude(int altitudeInFt);

  void setTargetHeading(IHeadingUpdater headingUpdater);

  void takeOff(ActiveRunwayThreshold threshold);

  void tuneAtc(AtcId atc);
}
