package eng.jAtcSim.newLib.atcs;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class LAcc {
  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    throw new ToDoException();
  }

  public static Messenger getMessenger() {
    return InstanceProviderDictionary.getInstance(Messenger.class);
  }

  public static IAirplane4Atc getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    throw new ToDoException();
  }

  public static PlaneResponsibilityManager getPrm(){
    return InstanceProviderDictionary.getInstance(PlaneResponsibilityManager.class);
  }
}
