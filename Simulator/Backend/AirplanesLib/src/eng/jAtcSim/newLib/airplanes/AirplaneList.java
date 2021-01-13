package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.airplanes.internal.Airplane;

public class AirplaneList extends BaseAirplaneList<Airplane> {
  public AirplaneList() {
    super(q -> q.getReader().getCallsign(), q -> q.getReader().getSqwk());
  }
}
