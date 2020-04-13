package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.airplanes.other.NewApproachInfo;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.HighOrderedSpeedForApproach;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedToApproachCommand;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ClearedToApproachCommand c) {
    Rejection ret = null;
    NewApproachInfo nai;

    ActiveRunwayThreshold rt = LAcc.getAirport().tryGetRunwayThreshold(c.getThresholdName());
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), plane.getType().category);
      if (apps.isEmpty())
        ret = new Rejection(
            "Cannot be cleared to approach. There is no approach kind "
                + c.getType() + " for runway " + rt.getName() + " for our plane.", c);
      else {
        nai = createApproachInfo(apps, plane);
        if (nai == null)
          ret = new UnableToEnterApproachFromDifficultPosition(c, "We are not in the correct position to enter the approach.");
      }
    }
    if (ret != null) return ret;

    //TODO here I should check if the approach can be started, but don't know
    //TODO how to pass an instance to for checking
//    for (IApproachStage stage : nai.getStages()) {
//      if (stage instanceof CheckApproachStage) {
//        CheckApproachStage.eCheckResult result = ((CheckApproachStage) stage).check(plane.);
//        if (result != CheckApproachStage.eCheckResult.ok) {
//          ret = new UnableToEnterApproachFromDifficultPosition(c, checkResultToString(result));
//          break;
//        }
//      } else break;
//    }

    throw new UnsupportedOperationException("Return here Null or \"Confirmation\"?");
//    if (ret == null)
//      ret = new Confirmation(c);
//
//    return ret;
  }

//  private String checkResultToString(IApproachStage.eResult result) {
//    throw new UnsupportedOperationException("Todo");
//  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ClearedToApproachCommand c) {
    ApplicationResult ret = new ApplicationResult();

    Restriction sr = plane.getSpeedRestriction();

    if (sr != null &&
        (sr.direction == AboveBelowExactly.above ||
            sr.direction == AboveBelowExactly.exactly) &&
        sr.value > plane.getType().vApp) {
      INotification tmp = new HighOrderedSpeedForApproach(sr.value, plane.getType().vApp);
      ret.informations.add(tmp);
    }

    ActiveRunwayThreshold rt = LAcc.getAirport().tryGetRunwayThreshold(c.getThresholdName());
    IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), plane.getType().category);
    NewApproachInfo nai = createApproachInfo(apps, plane);
    assert nai != null;

    plane.clearedToApproach(nai);

    return ret;
  }

  private NewApproachInfo createApproachInfo(IReadOnlyList<Approach> apps, IAirplaneCommand pilot) {
    NewApproachInfo ret = null;

    for (Approach approach : apps) {
      if (ret != null) break;
      for (ApproachEntry entry : approach.getEntries()) {
        if (ret != null) break;
        if (entry.getEntryLocation().isInside(pilot.getCoordinate())){
          ret = new NewApproachInfo(entry, approach);
        }
      }
    }

    return null;
  }
}
