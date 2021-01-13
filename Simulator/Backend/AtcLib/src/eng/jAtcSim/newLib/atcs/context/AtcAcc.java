package eng.jAtcSim.newLib.atcs.context;

import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.AtcIdList;
import eng.jAtcSim.newLib.shared.Callsign;

public class AtcAcc implements IAtcAcc {
  private final AtcIdList atcList;
  private final Selector<Callsign, AtcId> getResponsibleAtcSelector;

  public AtcAcc(AtcIdList atcList, Selector<Callsign, AtcId> getResponsibleAtcSelector) {
    this.atcList = atcList;
    this.getResponsibleAtcSelector = getResponsibleAtcSelector;
  }

  @Override
  public AtcIdList getAtcList() {
    return this.atcList;
  }

  @Override
  public AtcId getResponsibleAtcId(Callsign callsign) {
    return getResponsibleAtcSelector.invoke(callsign);
  }
}
