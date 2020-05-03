package eng.jAtcSim.newLib.airplanes.internal;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.airplanes.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.atc2airplane.ToNavaidCommand;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class InternalAcc {
  private static Producer<AirplaneList<Airplane>> airplanesProducer = null;

  public static Navaid getNavaid(ToNavaidCommand toNavaidCommand) {
    return AreaAcc.getNavaids().get(toNavaidCommand.getNavaidName());
  }

  public static void setAirplaneListProducer(Producer<AirplaneList<Airplane>> airplanesProducer) {
    InternalAcc.airplanesProducer = airplanesProducer;
  }

  public static AirplaneList<Airplane> getAirplanes() {
    return airplanesProducer.produce();
  }
}
