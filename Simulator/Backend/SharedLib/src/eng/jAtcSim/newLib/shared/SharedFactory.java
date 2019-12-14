package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.time.ERunningTime;
import eng.jAtcSim.newLib.shared.time.ETime;

public class SharedFactory {
  public static ETime getNow() {
    return Factory.getInstance(ERunningTime.class);
  }

  public static ERandom getRnd() {
    return Factory.getInstance(ERandom.class);
  }
}
