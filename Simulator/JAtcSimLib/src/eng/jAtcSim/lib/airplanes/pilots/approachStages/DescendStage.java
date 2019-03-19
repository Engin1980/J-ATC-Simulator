package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.world.Navaid;

public class DescendStage implements IApproachStage {
  private static final double THRESHOLD_DISTANCE_TO_EXIT_FIX = 1.5;
  private static final double TOO_HIGH_ALTITUDE_THRESHOLD = 600;
  private final Navaid navaid;
  private final int altitude;
  private final int radial;
  private final double slopeAngle;
  private final Navaid exitFix;
  private final Integer exitAltitude;

  public DescendStage(Navaid navaid, int altitude, int radial, double slopeAngle, Navaid exitFix, Integer exitAltitude) {
    Validator.check(exitFix != null || exitAltitude != null);
    this.navaid = navaid;
    this.altitude = altitude;
    this.radial = radial;
    this.slopeAngle = slopeAngle;
    this.exitFix = exitFix;
    this.exitAltitude = exitAltitude;
  }

  @Override
  public eResult initStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot) {
    eResult ret = updateAltitude(pilot);
    if (ret == eResult.ok)
      ret = updateHeading(pilot);
    return ret;
  }

  @Override
  public eResult disposeStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    if (exitFix != null) {
      double d = Coordinates.getDistanceInNM(pilot.getCoordinate(), navaid.getCoordinate());
      if (d < THRESHOLD_DISTANCE_TO_EXIT_FIX) return true;
    }
    if (exitAltitude != null)
      if (pilot.getAltitude() < exitAltitude) return true;
    return false;
  }

  private eResult updateHeading(IPilot4Behavior pilot) {
    double newHeading = Coordinates.getHeadingToRadial(pilot.getCoordinate(), navaid.getCoordinate(), this.radial, Coordinates.eHeadingToRadialBehavior.standard);
    pilot.setTargetHeading(newHeading);
    return eResult.ok;
  }

  private eResult updateAltitude(IPilot4Behavior pilot) {
    eResult ret;
    double distance = Coordinates.getDistanceInNM(pilot.getCoordinate(), navaid.getCoordinate());
    if (pilot.getTargetAltitude() < altitude) // already passed 'navaid'
      distance = -distance;

    double newAltitude = slopeAngle * distance / 60;
    newAltitude = UnitProvider.nmToFt(newAltitude) + altitude;
    newAltitude = Math.min(newAltitude, pilot.getTargetAltitude());

    if (pilot.getAltitude() - newAltitude > TOO_HIGH_ALTITUDE_THRESHOLD)
      ret = eResult.altitudeTooHigh;
    else
      ret = eResult.ok;

    pilot.setTargetAltitude(newAltitude);
    return ret;
  }
}
