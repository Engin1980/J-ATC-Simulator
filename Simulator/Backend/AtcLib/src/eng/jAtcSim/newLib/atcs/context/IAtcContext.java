package eng.jAtcSim.newLib.atcs.context;

import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public interface IAtcContext {
  AtcList<AtcId> getAtcList();
  AtcId getResponsibleAtcId(Callsign callsign);
}
