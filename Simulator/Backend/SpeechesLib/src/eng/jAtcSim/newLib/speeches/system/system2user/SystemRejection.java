package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.base.Rejection;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class SystemRejection extends Rejection<ISystemSpeech> implements ISystemNotification {
  public SystemRejection(ISystemSpeech origin, String reason) {
    super(origin, reason);
  }
}
