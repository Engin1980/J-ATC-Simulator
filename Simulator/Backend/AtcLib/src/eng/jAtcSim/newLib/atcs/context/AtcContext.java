package eng.jAtcSim.newLib.atcs.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public class AtcContext implements IAtcContext {
  private final AtcList<AtcId> atcList;
  private final Selector<Callsign, AtcId> getResponsibleAtcSelector;

  public AtcContext(AtcList<AtcId> atcList, Selector<Callsign, AtcId> getResponsibleAtcSelector) {
    this.atcList = atcList;
    this.getResponsibleAtcSelector = getResponsibleAtcSelector;
  }

  @Override
  public AtcList<AtcId> getAtcList() {
    return this.atcList;
  }

  @Override
  public AtcId getResponsibleAtcId(Callsign callsign) {
    return getResponsibleAtcSelector.getValue(callsign);
  }
}
