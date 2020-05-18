package eng.jAtcSim.newLib.airplaneType;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AirplaneTypeAcc {
  public static Producer<AirplaneTypes> airplaneTypeProducer;

  public static void setAirplaneTypeProducer(Producer<AirplaneTypes> airplaneTypeProducer) {
    AirplaneTypeAcc.airplaneTypeProducer = airplaneTypeProducer;
  }

  public static AirplaneTypes getAirplaneTypes(){
    return airplaneTypeProducer.produce();
  }
}
