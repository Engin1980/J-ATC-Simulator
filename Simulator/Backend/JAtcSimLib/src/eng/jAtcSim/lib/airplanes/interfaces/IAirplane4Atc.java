package eng.jAtcSim.lib.airplanes.interfaces;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.DARoute;

public interface IAirplane4Atc extends IAirplaneRO{
  void setHoldingPointState(ActiveRunwayThreshold threshold);
  void setRouting(DARoute r, ActiveRunwayThreshold runwayThreshold);
}
