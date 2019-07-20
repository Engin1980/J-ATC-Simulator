package eng.jAtcSim.lib.airplanes.interfaces;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Route;

public interface IAirplane4Atc extends IAirplaneRO{
  void setHoldingPointState(ActiveRunwayThreshold threshold);
  void setRouting(Route r, ActiveRunwayThreshold runwayThreshold);
}
