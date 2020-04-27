package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;

public class LAcc {
  public static Messenger getMessenger() {
    return InstanceProviderDictionary.getInstance(Messenger.class);
  }

  public static PlaneResponsibilityManager getPrm(){
    return InstanceProviderDictionary.getInstance(PlaneResponsibilityManager.class);
  }
}
