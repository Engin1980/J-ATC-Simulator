package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class TakeOffBehavior extends Behavior {

  //TODO add to airport config the acceleration altitude and use it here
  private final int accelerationAltitude;
  private ActiveRunwayThreshold toThreshold;

  @XmlConstructor
  private TakeOffBehavior() {
    accelerationAltitude = 0;
  }

  public TakeOffBehavior(IPilot4Behavior pilot, ActiveRunwayThreshold toThreshold) {

    this.toThreshold = toThreshold;
    int accAlt;
    switch (pilot.getAirplaneType().category) {
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
        throw new EEnumValueUnsupportedException(pilot.getAirplaneType().category);
    }
    this.accelerationAltitude = Acc.airport().getAltitude() + accAlt;
  }

  @Override
  public void fly(IPilot4Behavior pilot) {
    switch (pilot.getState()) {
      case holdingPoint:
        break;
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            pilot.getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
        pilot.setTargetHeading(targetHeading);

        if (pilot.getSpeed() > pilot.getAirplaneType().vR) {
          super.setBehaviorAndState(pilot, this, Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (pilot.getAltitude() > this.accelerationAltitude)
          if (pilot.isArrival()) {
            // antecedent G/A
            super.setBehaviorAndState(pilot, new ArrivalBehavior(), Airplane.State.arrivingHigh);
          } else {
            super.setBehaviorAndState(
                pilot,
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
