package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.Producer;

public class AirplaneAcc {

  private static Producer<AirplaneList> airplaneListProducer = null;

  public static void setAirplaneListProducer(Producer<AirplaneList> airplaneListProducer) {
    AirplaneAcc.airplaneListProducer = airplaneListProducer;
  }

  public static AirplaneList getAirplaneList() {
    return airplaneListProducer.produce();
  }
}
