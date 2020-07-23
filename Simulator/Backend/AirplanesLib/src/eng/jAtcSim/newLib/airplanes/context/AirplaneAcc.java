package eng.jAtcSim.newLib.airplanes.context;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;

public class AirplaneAcc implements IAirplaneAcc {

  private final AirplanesController airplanesController;

  public AirplaneAcc(AirplanesController airplanesController) {
    EAssert.Argument.isNotNull(airplanesController, "airplanesController");
    this.airplanesController = airplanesController;
  }

  @Override
  public AirplanesController getAirplanesController() {
    return airplanesController;
  }
}
