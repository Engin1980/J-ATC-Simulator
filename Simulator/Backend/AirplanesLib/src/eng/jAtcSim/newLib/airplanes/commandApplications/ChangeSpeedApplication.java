package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeSpeedCommand;

public class ChangeSpeedApplication extends CommandApplication<ChangeSpeedCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ChangeSpeedCommand c) {
    Rejection ret;

    if (c.isResumeOwnSpeed() == false) {
      // not resume speed

      boolean isInApproach = plane.getState().is(
          Airplane.State.approachEnter,
          Airplane.State.approachDescend
      );

      Restriction r = c.getRestriction();
      int cMax = !isInApproach ? plane.getType().vMaxClean : plane.getType().vMaxApp;
      int cMin = !isInApproach ? plane.getType().vMinClean : plane.getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(
          plane.getCoordinate(), LAcc.getAirport().getLocation()) < 20) {
        //cMin = (int) (cMin * 0.85);
        cMin = plane.getType().vMaxApp;
      }

      if (r.direction != AboveBelowExactly.below && r.value > cMax) {
        ret = new Rejection("Unable to reach speed " + r.value + " kts, maximum is " + cMax, c);
        return ret;
      } else if (r.direction != AboveBelowExactly.above && r.value < cMin) {
        ret = new Rejection("Unable to reach speed " + r.value + " kts, minimum is " + cMin , c);
        return ret;
      }
    }

    return null;

  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ChangeSpeedCommand c) {
    plane.setSpeedRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
