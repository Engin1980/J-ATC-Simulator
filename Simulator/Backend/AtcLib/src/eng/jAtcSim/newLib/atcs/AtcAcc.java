package eng.jAtcSim.newLib.atcs;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AtcAcc {
  private static Producer<AtcList> atcListProducer = null;

  public static void setAtcListProducer(Producer<AtcList> atcListProducer) {
    AtcAcc.atcListProducer = atcListProducer;
  }

  public static AtcList getAtcList() {
    return atcListProducer.produce();
  }
}
