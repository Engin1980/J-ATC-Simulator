package eng.jAtcSim.newLib.area.airplanes.interfaces;

import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.world.DARoute;

public interface IAirplane4Atc extends IAirplaneRO{
  void setHoldingPointState(ActiveRunwayThreshold threshold);
  void setRouting(DARoute r, ActiveRunwayThreshold runwayThreshold);
}
