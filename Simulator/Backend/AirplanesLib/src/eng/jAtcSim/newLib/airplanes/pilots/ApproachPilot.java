package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRouteBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.RadialCalculator;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ThenCommand;

public class ApproachPilot extends Pilot {

  private static final int MAX_HEADING_DIFFERENCE = 40;
  private final Approach approach;
  private final IafRoute iafRoute;
  private Integer currentStageIndex = null;

  public ApproachPilot(IPlaneInterface plane,
                       Approach approach, ApproachEntry entry) {
    super(plane);
    EAssert.Argument.isNotNull(approach, "approach");
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isTrue(approach.getEntries().contains(entry));
    EAssert.Argument.isTrue(entry.getEntryLocation().isInside(plane.getCoordinate()));

    this.approach = approach;
    this.iafRoute = entry.getIafRoute() == null ? null : entry.getIafRoute().createClone();
  }

  public SpeechList<ICommand> getGoAroundRouting() {
    SpeechList<ICommand> ret = new SpeechList<>(this.approach.getGaRoute().getRouteCommands());
    ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
    if (ret.get(0) instanceof ChangeAltitudeCommand) {
      cac = (ChangeAltitudeCommand) ret.get(0);
      ret.removeAt(0);
    }
    ret.insert(0,
        ChangeHeadingCommand.create((int) this.getRunwayThreshold().getCourse(), LeftRightAny.any));

    // check if is before runway threshold.
    // if is far before, then first point will still be runway threshold
    if (isBeforeRunwayThreshold()) {
      Navaid runwayThresholdNavaid = LAcc.getNavaids().addRunwayThresholdPoint(
          this.getRunwayThreshold().getParent().getParent().getIcao(),
          this.getRunwayThreshold().getName(),
          this.getRunwayThreshold().getCoordinate()
      );
      ret.insert(0, ProceedDirectCommand.create(runwayThresholdNavaid.getName()));
      ret.insert(1, ThenCommand.create());
    }

    if (cac != null)
      ret.insert(0, cac);

    return ret;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return approach.getParent();
  }

  @Override
  public boolean isDivertable() {
    return plane.getState() != Airplane.State.approachDescend;
  }

  @Override
  protected void elapseSecondInternal() {
    switch (plane.getState()) {
      case arrivingHigh:
      case arrivingLow:
        plane.setRouting(iafRoute, approach.getParent());
        plane.setState(Airplane.State.flyingIaf2Faf);
        break;
      case flyingIaf2Faf:
        if (plane.isRoutingEmpty() && currentStageIndex == null) {
          plane.setRouting(approach.getBeforeStagesCommands());
          currentStageIndex = -1;
        } else if (plane.isRoutingEmpty() && currentStageIndex != null) {
          plane.setState(Airplane.State.approachEnter);
          setNextStage();
          flyStage();
        }
        break;
      case approachEnter:
      case approachDescend:
      case longFinal:
      case shortFinal:
        flyStage();
        break;
      case landed:
        break;
      default:
        throw new EEnumValueUnsupportedException(plane.getState());
    }
  }

  private void flyRadialBehavior(FlyRadialBehavior behavior) {
    if (behavior instanceof FlyRadialWithDescentBehavior) {
      flyRadialWithDescentBehavior((FlyRadialWithDescentBehavior) behavior);
    }
    double heading = RadialCalculator.getHeadingToFollowRadial(
        plane.getCoordinate(), behavior.getCoordinate(), behavior.getInboundRadial(),
        MAX_HEADING_DIFFERENCE, plane.getSpeed());

    plane.setTargetHeading(
        new HeadingNavigator(heading, LeftRightAny.any));
  }

  private void flyRadialWithDescentBehavior(FlyRadialWithDescentBehavior behavior) {
    double distance = Coordinates.getDistanceInNM(behavior.getCoordinate(), plane.getCoordinate());
    double altitudeDouble = behavior.getAltitudeFixValue() + behavior.getSlope() * distance;
    int altitudeInt = (int) Math.round(altitudeDouble);
    plane.setTargetAltitude(altitudeInt);
  }

  private void flyStage() {
    EAssert.isNotNull(currentStageIndex);
    ApproachStage stage = approach.getStages().get(currentStageIndex);

    if (isConditionTrue(stage.getExitCondition())) {
      // go to next stage
      setNextStage();
      return;
    }

    IApproachBehavior beh = stage.getBehavior();
    if (beh instanceof FlyRadialBehavior)
      flyRadialBehavior((FlyRadialBehavior) beh);
    else if (beh instanceof FlyRouteBehavior) {
      // nothing, everything already done in "setNextStep()"
    } else
      throw new EApplicationException("Unknown behavior type at this place.");

    if (isConditionTrue(stage.getErrorCondition()))
      goAround(GoingAroundNotification.GoAroundReason.notStabilizedAirplane);
  }

  @Override
  protected Airplane.State[] getInitialStates() {
    return new Airplane.State[]{
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow
    };
  }

  @Override
  protected Airplane.State[] getValidStates() {
    return new Airplane.State[]{
        Airplane.State.arrivingCloseFaf,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  private void goAround(GoingAroundNotification.GoAroundReason reason) {
    plane.goAround(reason);
  }

  private boolean isBeforeRunwayThreshold() {
    double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    double hdg = Coordinates.getBearing(plane.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    boolean ret;
    if (dist < 3)
      ret = false;
    else {
      ret = Headings.isBetween(this.getRunwayThreshold().getCourse() - 70, hdg, this.getRunwayThreshold().getCourse() + 70);
    }
    return ret;
  }

  private boolean isConditionTrue(ICondition condition) {
    return ConditionEvaluator.check(condition, plane);
  }

  private void setNextStage() {
    this.currentStageIndex++;
    EAssert.isTrue(currentStageIndex >= 0);
    ApproachStage stage = approach.getStages().get(currentStageIndex);
    IApproachBehavior beh = stage.getBehavior();
    if (beh instanceof FlyRouteBehavior) {
      plane.setRouting(((FlyRouteBehavior) beh).getCommands());
    }

  }
}
