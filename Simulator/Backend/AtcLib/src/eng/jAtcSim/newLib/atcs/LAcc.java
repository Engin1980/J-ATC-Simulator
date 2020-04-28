package eng.jAtcSim.newLib.atcs;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class LAcc {


  public static Messenger getMessenger() {
    return InstanceProviderDictionary.getInstance(Messenger.class);
  }

  public static PlaneResponsibilityManager getPrm(){
    return InstanceProviderDictionary.getInstance(PlaneResponsibilityManager.class);
  }
}
