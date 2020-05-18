package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.functionalInterfaces.Producer;

public class AirplaneAcc {

  private static Producer<AirplaneList<IAirplane>> airplanesProducer = null;
  private static Producer<AirplanesController> airplanesControllerProducer = null;

  public static AirplaneList<IAirplane> getAirplanes() {
    return airplanesProducer.produce();
  }

  public static AirplanesController getAirplanesController() {
    return airplanesControllerProducer.produce();
  }

  public static boolean isSomeActiveEmergency() {
    return getAirplanes().isAny(q -> q.isEmergency());
  }

  static void setAirplaneListProducer(Producer<AirplaneList<IAirplane>> airplanesProducer) {
    AirplaneAcc.airplanesProducer = airplanesProducer;
  }

  static void setAirplanesControllerProducer(Producer<AirplanesController> airplanesControllerProducer) {
    AirplaneAcc.airplanesControllerProducer = airplanesControllerProducer;
  }
}
