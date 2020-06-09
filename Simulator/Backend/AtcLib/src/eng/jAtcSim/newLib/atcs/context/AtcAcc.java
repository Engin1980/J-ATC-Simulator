package eng.jAtcSim.newLib.atcs.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public class AtcAcc {
  private static Producer<AtcList<AtcId>> atcListProducer = null;
  private static Selector<Callsign, AtcId> getResponsibleAtcSelector = null;

  public static AtcList<AtcId> getAtcList() {
    return atcListProducer.produce();
  }

  public static AtcId getResponsibleAtcId(Callsign callsign) {
    return getResponsibleAtcSelector.getValue(callsign);
  }

  public static void setAtcListProducer(Producer<AtcList<AtcId>> atcListProducer) {
    AtcAcc.atcListProducer = atcListProducer;
  }
}
