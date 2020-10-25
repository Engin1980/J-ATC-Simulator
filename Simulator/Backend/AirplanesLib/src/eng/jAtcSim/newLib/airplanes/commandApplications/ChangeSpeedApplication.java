package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeSpeedCommand;

public class ChangeSpeedApplication extends CommandApplication<ChangeSpeedCommand> {

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ChangeSpeedCommand c) {
    PlaneRejection ret;

    if (c.isResumeOwnSpeed() == false) {
      // not resume speed

      boolean isInApproach = plane.getReader().getState().is(
          AirplaneState.approachEnter,
          AirplaneState.approachDescend
      );

      Restriction r = c.getRestriction();
      int cMax = !isInApproach ? plane.getReader().getType().vMaxClean : plane.getReader().getType().vMaxApp;
      int cMin = !isInApproach ? plane.getReader().getType().vMinClean : plane.getReader().getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(
          plane.getReader().getCoordinate(), Context.getArea().getAirport().getLocation()) < 20) {
        //cMin = (int) (cMin * 0.85);
        cMin = plane.getReader().getType().vMaxApp;
      }

      if (r.direction != AboveBelowExactly.below && r.value > cMax) {
        ret = new PlaneRejection(c,"Unable to reach speed " + r.value + " kts, maximum is " + cMax);
        return ret;
      } else if (r.direction != AboveBelowExactly.above && r.value < cMin) {
        ret = new PlaneRejection(c,"Unable to reach speed " + r.value + " kts, minimum is " + cMin );
        return ret;
      }
    }

    return null;

  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed())
      plane.getWriter().setSpeedRestriction(null);
    else
      plane.getWriter().setSpeedRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
