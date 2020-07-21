package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.ERandom;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public interface ISharedContext {
  String getAirportIcao();
  IReadOnlyList<AtcId> getAtcs();
  String getLogPath();
  EDayTimeRun getNow();
  SimulationLog getSimLog();
}
