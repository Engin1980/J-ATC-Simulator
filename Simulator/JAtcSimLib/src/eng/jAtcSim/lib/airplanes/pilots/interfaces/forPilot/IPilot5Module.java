package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.modules.ShaModule;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.airplanes.pilots.modules.BehaviorModule;
import eng.jAtcSim.lib.airplanes.pilots.modules.PilotRecorderModule;

public interface IPilot5Module extends IPilot5 {
  PilotRecorderModule getRecorderModule();
  ShaModule getSha();
  BehaviorModule getBehaviorModule();
  IPilot5Command getPilot5Command();
  IAirplaneRO getPlane();
}
