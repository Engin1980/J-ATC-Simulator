package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.ERandom;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public class AppContext implements IAppContext {
  private final ApplicationLog applicationLog;
  private final ERandom rnd = new ERandom();

  public AppContext(ApplicationLog applicationLog) {
    EAssert.Argument.isNotNull(applicationLog, "applicationLog");
    this.applicationLog = applicationLog;
  }

  @Override
  public ERandom getRnd() {
    return rnd;
  }

  @Override
  public ApplicationLog getAppLog() {
    return this.applicationLog;
  }
}
