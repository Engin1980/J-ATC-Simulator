package eng.jAtcSim.newLib.airplanes.behaviors;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class TakeOffBehavior extends Behavior {

  //TODO add to airport config the acceleration altitude and use it here
  private final int accelerationAltitude;
  private ActiveRunwayThreshold toThreshold;

  @XmlConstructor
  private TakeOffBehavior() {
    accelerationAltitude = 0;
  }

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
  public void fly(IAirplaneWriteSimple plane) {
    switch (plane.getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            plane.getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
        plane.setTargetHeading(targetHeading);

        if (plane.getSha().getSpeed() > plane.getType().vR) {
          plane.setBehaviorAndState(this, Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (plane.getSha().getAltitude() > this.accelerationAltitude)
          if (plane.getFlightModule().isArrival()) {
            // antecedent G/A
            plane.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
          } else {
            plane.setBehaviorAndState(
                new DepartureBehavior(),
                Airplane.State.departingLow
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
