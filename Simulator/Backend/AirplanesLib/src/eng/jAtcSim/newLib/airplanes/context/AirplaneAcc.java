//package eng.jAtcSim.newLib.airplanes.context;
//
//import eng.eSystem.functionalInterfaces.Producer;
//import eng.jAtcSim.newLib.airplanes.AirplaneList;
//import eng.jAtcSim.newLib.airplanes.AirplanesController;
//import eng.jAtcSim.newLib.airplanes.IAirplane;
//
//public class AirplaneAcc {
//
//  private static Producer<AirplaneList<IAirplane>> airplanesProducer = null;
//  private static Producer<AirplanesController> airplanesControllerProducer = null;
//
//  public static AirplaneList<IAirplane> getAirplanes() {
//    return airplanesProducer.produce();
//  }
//
//  public static AirplanesController getAirplanesController() {
//    return airplanesControllerProducer.produce();
//  }
//
//  public static boolean isSomeActiveEmergency() {
//    return getAirplanes().isAny(q -> q.isEmergency());
//  }
//
//  public static void setAirplaneListProducer(Producer<AirplaneList<IAirplane>> airplanesProducer) {
//    AirplaneAcc.airplanesProducer = airplanesProducer;
//  }
//
//  public static void setAirplanesControllerProducer(Producer<AirplanesController> airplanesControllerProducer) {
//    AirplaneAcc.airplanesControllerProducer = airplanesControllerProducer;
//  }
//}
