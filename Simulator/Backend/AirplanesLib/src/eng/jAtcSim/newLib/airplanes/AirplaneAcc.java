package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.Producer;

public class AirplaneAcc {

  private static Producer<AirplaneList<IAirplane>> airplanesProducer = null;

  public static boolean isSomeActiveEmergency() {
    return getAirplanes().isAny(q->q.isEmergency());
  }

  public static void setAirplaneListProducer(Producer<AirplaneList<IAirplane>> airplanesProducer) {
    AirplaneAcc.airplanesProducer = airplanesProducer;
  }

  public static AirplaneList<IAirplane> getAirplanes() {
    return airplanesProducer.produce();
  }
}
