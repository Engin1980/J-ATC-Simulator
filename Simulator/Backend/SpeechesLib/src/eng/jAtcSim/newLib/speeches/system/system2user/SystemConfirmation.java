package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.jAtcSim.newLib.speeches.base.Confirmation;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

public class SystemConfirmation extends Confirmation<ISystemUserRequest> implements ISystemNotification {
  public SystemConfirmation(ISystemUserRequest origin) {
    super(origin);
  }
}
