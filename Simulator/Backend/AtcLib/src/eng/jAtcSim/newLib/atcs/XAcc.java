package eng.jAtcSim.newLib.atcs;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class XAcc {
  public static IAirplane4Atc getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    throw new ToDoException();
  }

  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    throw new ToDoException();
  }
}
