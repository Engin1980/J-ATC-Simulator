package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.base.Confirmation;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class SystemConfirmation extends Confirmation<ISystemUserRequest> implements ISystemNotification {
  public SystemConfirmation(ISystemUserRequest origin) {
    super(origin);
  }
}
