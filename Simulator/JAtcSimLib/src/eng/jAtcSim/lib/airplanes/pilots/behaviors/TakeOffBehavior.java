package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

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
  public void fly(IPilotWriteSimple pilot) {
    switch (pilot.getPlane().getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            pilot.getPlane().getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
        pilot.setTargetHeading(targetHeading);

        if (pilot.getPlane().getSha().getSpeed() > pilot.getPlane().getType().vR) {
          pilot.setBehaviorAndState(this, Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (pilot.getPlane().getSha().getAltitude() > this.accelerationAltitude)
          if (pilot.getPlane().getFlight().isArrival()) {
            // antecedent G/A
            pilot.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
          } else {
            pilot.setBehaviorAndState(
                new DepartureBehavior(),
                Airplane.State.departingLow
            );
          }
        break;
      default:
        super.throwIllegalStateException(pilot);
    }
  }

  @Override
  public String toLogString() {
    return "TKO";
  }

}
