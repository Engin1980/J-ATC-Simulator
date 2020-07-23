package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import java.nio.file.Path;

public interface IAppAcc {
  ApplicationLog getAppLog();

  Path getLogPath();

  ERandom getRnd();
}
