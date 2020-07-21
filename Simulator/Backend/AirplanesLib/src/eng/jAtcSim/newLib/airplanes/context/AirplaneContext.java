package eng.jAtcSim.newLib.airplanes.context;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.IAirplane;

public class AirplaneContext implements IAirplaneContext {

  private final AirplaneList<IAirplane> airplanes;
  private final AirplanesController airplanesController;

  public AirplaneContext(AirplaneList<IAirplane> airplanes, AirplanesController airplanesController) {
    EAssert.Argument.isNotNull(airplanes, "airplanes");
    EAssert.Argument.isNotNull(airplanesController, "airplanesController");
    this.airplanes = airplanes;
    this.airplanesController = airplanesController;
  }

  @Override
  public AirplaneList<IAirplane> getAirplanes() {
    return null;
  }

  @Override
  public AirplanesController getAirplanesController() {
    return null;
  }
}
