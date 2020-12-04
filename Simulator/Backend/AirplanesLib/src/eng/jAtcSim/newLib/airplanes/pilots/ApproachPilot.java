package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
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
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;

public class ApproachPilot extends Pilot {

  private static final int MAX_HEADING_DIFFERENCE = 40;

  public static ApproachPilot createEmptyToLoad(Airplane airplane) {
    return new ApproachPilot(airplane);
  }
  private final Approach approach;
  private final IafRoute iafRoute;
  private Integer currentStageIndex = null;

  //TODO this should be done via @XmlConstructor
  private ApproachPilot(Airplane airplane) {
    super(airplane);
    this.approach = null;
    this.iafRoute = null;
  }

  public ApproachPilot(Airplane plane,
                       Approach approach, ApproachEntry entry) {
    super(plane);
    EAssert.Argument.isNotNull(approach, "approach");
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isTrue(approach.getEntries().contains(entry));
    EAssert.Argument.isTrue(entry.getEntryLocation().isInside(plane.getReader().getCoordinate()));

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
      Navaid runwayThresholdNavaid = Context.getArea().getNavaids().addRunwayThresholdPoint(
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
    return rdr.getState() != AirplaneState.approachDescend;
  }

  @Override
  protected void elapseSecondInternal() {
    switch (rdr.getState()) {
      case arrivingHigh:
      case arrivingLow:
        wrt.setRouting(iafRoute, approach.getParent());
        wrt.setState(AirplaneState.flyingIaf2Faf);
        break;
      case flyingIaf2Faf:
        if (rdr.getRouting().isRoutingEmpty() && currentStageIndex == null) {
          wrt.setRouting(approach.getBeforeStagesCommands());
          currentStageIndex = -1;
        } else if (rdr.getRouting().isRoutingEmpty() && currentStageIndex != null) {
          wrt.setState(AirplaneState.approachEnter);
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
        throw new EEnumValueUnsupportedException(rdr.getState());
    }
  }

  private void flyRadialBehavior(FlyRadialBehavior behavior) {
    if (behavior instanceof FlyRadialWithDescentBehavior) {
      flyRadialWithDescentBehavior((FlyRadialWithDescentBehavior) behavior);
    }
    double heading = RadialCalculator.getHeadingToFollowRadial(
            rdr.getCoordinate(), behavior.getCoordinate(), behavior.getInboundRadial(),
            MAX_HEADING_DIFFERENCE, rdr.getSha().getSpeed());

    wrt.setTargetHeading(
            new HeadingNavigator(heading, LeftRightAny.any));
  }

  private void flyRadialWithDescentBehavior(FlyRadialWithDescentBehavior behavior) {
    double distance = Coordinates.getDistanceInNM(behavior.getCoordinate(), rdr.getCoordinate());
    double altitudeDouble = behavior.getAltitudeFixValue() + behavior.getSlope() * distance;
    int altitudeInt = (int) Math.round(altitudeDouble);
    wrt.setTargetAltitude(altitudeInt);
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
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.arrivingHigh,
            AirplaneState.arrivingLow,
            AirplaneState.flyingIaf2Faf
    };
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{
            AirplaneState.arrivingCloseFaf,
            AirplaneState.flyingIaf2Faf,
            AirplaneState.approachEnter,
            AirplaneState.approachDescend,
            AirplaneState.longFinal,
            AirplaneState.shortFinal,
            AirplaneState.landed
    };
  }

  private void goAround(GoingAroundNotification.GoAroundReason reason) {
    wrt.goAround(reason);
  }

  private boolean isBeforeRunwayThreshold() {
    double dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    double hdg = Coordinates.getBearing(rdr.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    boolean ret;
    if (dist < 3)
      ret = false;
    else {
      ret = Headings.isBetween(this.getRunwayThreshold().getCourse() - 70, hdg, this.getRunwayThreshold().getCourse() + 70);
    }
    return ret;
  }

  private boolean isConditionTrue(ICondition condition) {
    return ConditionEvaluator.check(condition, rdr);
  }

  private void setNextStage() {
    this.currentStageIndex++;
    EAssert.isTrue(currentStageIndex >= 0);
    ApproachStage stage = approach.getStages().get(currentStageIndex);
    IApproachBehavior beh = stage.getBehavior();
    if (beh instanceof FlyRouteBehavior) {
      wrt.setRouting(((FlyRouteBehavior) beh).getCommands());
    }
  }
}
