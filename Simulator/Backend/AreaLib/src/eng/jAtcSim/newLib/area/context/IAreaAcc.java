package eng.jAtcSim.newLib.area.context;

import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.RunwayConfiguration;

public interface IAreaAcc {
  Airport getAirport();

  Area getArea();

  RunwayConfiguration getCurrentRunwayConfiguration();

  RunwayConfiguration tryGetScheduledRunwayConfiguration();

  NavaidList getNavaids();

  EventAnonymousSimple onCurrentRunwayConfigurationChange();

  EventAnonymousSimple onScheduledRunwayConfigurationChange();

  void setCurrentRunwayConfiguration(RunwayConfiguration currentRunwayConfiguration);

  void setScheduledRunwayConfiguration(RunwayConfiguration scheduledRunwayConfiguration);
}
