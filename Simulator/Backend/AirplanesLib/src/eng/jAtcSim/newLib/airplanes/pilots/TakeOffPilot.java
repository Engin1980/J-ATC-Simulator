package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class TakeOffPilot implements IPilot {
  //TODO add to airport config the acceleration altitude and use it here
  private ActiveRunwayThreshold toThreshold;

  public TakeOffBehavior(char planeCategory, ActiveRunwayThreshold toThreshold) {

    this.toThreshold = toThreshold;
    int accAlt;
    switch (planeCategory) {
      case 'A':
        accAlt = 300;
        break;
      case 'B':
        accAlt = 1000;
        break;
      case 'C':
      case 'D':
        accAlt = 1500;
        break;
      default:
        throw new EEnumValueUnsupportedException(planeCategory);
    }
    this.accelerationAltitude = Acc.airport().getAltitude() + accAlt;
  }

  @Override
  public void fly(eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple plane) {
    switch (plane.getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            plane.getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
        plane.setTargetHeading(targetHeading);

        if (plane.getSha().getSpeed() > plane.getType().vR) {
          plane.setBehaviorAndState(this, eng.jAtcSim.newLib.area.airplanes.Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (plane.getSha().getAltitude() > this.accelerationAltitude)
          if (plane.getFlightModule().isArrival()) {
            // antecedent G/A
            plane.setBehaviorAndState(new ArrivalBehavior(), eng.jAtcSim.newLib.area.airplanes.Airplane.State.arrivingHigh);
          } else {
            plane.setBehaviorAndState(
                new DepartureBehavior(),
                eng.jAtcSim.newLib.area.airplanes.Airplane.State.departingLow
            );
          }
        break;
      default:
        super.throwIllegalStateException(plane);
    }
  }

  @Override
  public String toLogString() {
    return "TKO";
  }
}
