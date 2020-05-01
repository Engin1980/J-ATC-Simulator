package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public interface IAirplane4Atc {
  Callsign getCallsign();

  String getSqwk();

  AtcId getTunedAtc();
}
