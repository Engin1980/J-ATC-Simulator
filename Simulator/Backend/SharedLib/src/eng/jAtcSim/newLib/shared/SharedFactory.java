package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class SharedFactory {
  public static EDayTimeRun getNow() {
    return Factory.getInstance(EDayTimeRun.class);
  }

  public static ERandom getRnd() {
    return Factory.getInstance(ERandom.class);
  }
}
