package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class LAcc {
  public static Messenger getMessenger() {
    return InstanceProviderDictionary.getInstance(Messenger.class);
  }
}
