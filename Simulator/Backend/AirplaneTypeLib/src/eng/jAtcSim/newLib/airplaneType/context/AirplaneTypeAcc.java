package eng.jAtcSim.newLib.airplaneType.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AirplaneTypeAcc implements IAirplaneTypeAcc {
  public final AirplaneTypes airplaneTypes;

  public AirplaneTypeAcc(AirplaneTypes airplaneTypes) {
    EAssert.Argument.isNotNull(airplaneTypes, "airplaneTypes");
    this.airplaneTypes = airplaneTypes;
  }

  @Override
  public AirplaneTypes getAirplaneTypes() {
    return this.airplaneTypes;
  }
}
