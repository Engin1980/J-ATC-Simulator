package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.ERandom;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AppAcc implements IAppAcc {
  private final ApplicationLog applicationLog;
  private Path logPath;
  private final ERandom rnd = new ERandom();

  public AppAcc(ApplicationLog applicationLog, Path logPath) {
    EAssert.Argument.isNotNull(applicationLog, "applicationLog");
    EAssert.Argument.isNotNull(logPath, "logPath");
    this.logPath = logPath;
    this.applicationLog = applicationLog;
  }

  @Override
  public ApplicationLog getAppLog() {
    return this.applicationLog;
  }

  @Override
  public Path getLogPath() {
    return this.logPath;
  }

  @Override
  public ERandom getRnd() {
    return rnd;
  }

  public void updateLogPath(Path logFolder) {
    EAssert.Argument.isNotNull(logFolder, "logFolder");
    this.logPath = logFolder;
    this.applicationLog.updateOutputFilePath(
            Paths.get(
                    logFolder.toString(),
                    "app_log.txt"
            ).toString()
    );
  }
}
