package eng.jAtcSim.newLib.atcs;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.AtcId;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AtcAcc {
  private static Producer<AtcList<AtcId>> atcListProducer = null;

  public static void setAtcListProducer(Producer<AtcList<AtcId>> atcListProducer) {
    AtcAcc.atcListProducer = atcListProducer;
  }

  public static AtcList<AtcId> getAtcList() {
    return atcListProducer.produce();
  }
}
