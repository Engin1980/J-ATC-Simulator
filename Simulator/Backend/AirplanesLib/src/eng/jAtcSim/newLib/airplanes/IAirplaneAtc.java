package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.shared.AtcId;

public interface IAirplaneAtc {
  AtcId getTunedAtc();

  boolean hasRadarContact();
}
