package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public interface IAppContext {
  ApplicationLog getAppLog();
  ERandom getRnd();
}
