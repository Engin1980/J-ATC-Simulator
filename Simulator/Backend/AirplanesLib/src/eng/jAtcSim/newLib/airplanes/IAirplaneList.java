package eng.jAtcSim.newLib.airplanes;

public class IAirplaneList extends BaseAirplaneList<IAirplane> {
  public IAirplaneList() {
    super(q -> q.getCallsign(), q -> q.getSqwk());
  }
}
