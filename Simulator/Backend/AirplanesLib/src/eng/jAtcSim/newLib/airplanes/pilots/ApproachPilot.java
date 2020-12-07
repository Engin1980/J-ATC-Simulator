package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
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
import eng.jAtcSim.newLib.area.approaches.behaviors.*;
import eng.jAtcSim.newLib.area.approaches.conditions.FlyRouteBehaviorEmptyCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.area.approaches.conditions.NeverCondition;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.RadialCalculator;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
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
  private static final int FLARE_HEIGHT = 100;
  public static final int SHORT_FINAL_HEIGHT = 1000;
  public static final int SHORT_FINAL_DISTANCE = 3;
  public static final int LONG_FINAL_HEIGHT = 2000;
  public static final int LONG_FINAL_DISTANCE = 6;

  public static ApproachPilot createEmptyToLoad(Airplane airplane) {
    return new ApproachPilot(airplane);
  }

  private ApproachType type;
  private final IList<ApproachStage> stages;
  private final IList<ICommand> gaRouteCommands;
  private final ActiveRunwayThreshold threshold;
  private final int initialAltitude;

  //TODO this should be done via @XmlConstructor
  private ApproachPilot(Airplane airplane) {
    super(airplane);
    this.stages = null;
    this.gaRouteCommands = null;
    this.threshold = null;
    this.initialAltitude = 0;

    PostContracts.register(this, () -> stages != null);
    PostContracts.register(this, () -> gaRouteCommands != null);
    PostContracts.register(this, () -> threshold != null);
  }

  public ApproachPilot(Airplane plane,                       Approach approach, ApproachEntry entry) {
    super(plane);
    EAssert.Argument.isNotNull(approach, "approach");
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isTrue(approach.getEntries().contains(entry));

    this.threshold = approach.getParent();
    this.type = approach.getType();
    this.stages = new EList<>(approach.getStages());
    this.initialAltitude = approach.getInitialAltitude();

    if (entry.getIafRoute() != null) {
      FlyIafRouteBehavior frb = new FlyIafRouteBehavior(entry.getIafRoute());
      ApproachStage iafStage = ApproachStage.create(
              frb,
              new FlyRouteBehaviorEmptyCondition(),
              new NeverCondition(),
              "IAF via " + entry.getIafRoute().getNavaid());
      this.stages.insert(0, iafStage);
    }

    if (approach.getBeforeStagesCommands().isEmpty() == false) {
      FlyRouteBehavior frb = new FlyRouteBehavior(approach.getBeforeStagesCommands().toList());
      ApproachStage iafStage = ApproachStage.create(
              frb,
              new FlyRouteBehaviorEmptyCondition(),
              new NeverCondition(),
              "Initial approach commands");
      this.stages.insert(0, iafStage);
    }

    this.gaRouteCommands = approach.getGaRoute().getRouteCommands().toList();
  }

  public SpeechList<ICommand> getGoAroundRouting() {
    SpeechList<ICommand> ret = new SpeechList<>(this.gaRouteCommands);
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
    return threshold;
  }

  @Override
  public boolean isDivertable() {
    return rdr.getState() != AirplaneState.approachDescend;
  }

  @Override
  protected void elapseSecondInternal() {
    flyStage();
  }

  private void flyRadialBehavior(FlyRadialBehavior behavior) {
    int alt = rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude();
    if (alt > FLARE_HEIGHT && behavior instanceof FlyRadialWithDescentBehavior) {
      flyRadialWithDescentBehavior((FlyRadialWithDescentBehavior) behavior);
    }
    double heading = RadialCalculator.getHeadingToFollowRadial(
            rdr.getCoordinate(), behavior.getCoordinate(), behavior.getInboundRadialWithDeclination(),
            MAX_HEADING_DIFFERENCE, rdr.getSha().getSpeed());

    wrt.setTargetHeading(new HeadingNavigator(heading, LeftRightAny.any));
  }

  private void updateApproachState() {
    AirplaneState newState;
    if (this.stages.getFirst().getBehavior() instanceof FlyIafRouteBehavior)
      newState = AirplaneState.flyingIaf2Faf;
    else if (rdr.getState() == AirplaneState.landed || rdr.getState() == AirplaneState.shortFinal)
      newState = rdr.getState();
    else {
      if (rdr.getSha().getAltitude() > initialAltitude)
        newState = AirplaneState.approachEntry;
      else {
        int alt = rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude();
        double dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), rdr.getRouting().getAssignedRunwayThreshold().getCoordinate());
        if (alt < SHORT_FINAL_HEIGHT && dist < SHORT_FINAL_DISTANCE)
          newState = AirplaneState.shortFinal;
        else if (alt < LONG_FINAL_HEIGHT && dist < LONG_FINAL_DISTANCE)
          newState = AirplaneState.longFinal;
        else
          newState = AirplaneState.approachEntry;
      }
    }

    if (rdr.getState() != newState) wrt.setState(newState);
  }

  private void flyRadialWithDescentBehavior(FlyRadialWithDescentBehavior behavior) {
    double distance = Coordinates.getDistanceInNM(behavior.getCoordinate(), rdr.getCoordinate());
    double altitudeDouble = behavior.getAltitudeFixValue() + behavior.getSlope() * 6076.1 * distance; // http://www.aviationchief.com/ils.html
    int altitudeInt = (int) Math.round(altitudeDouble);
    if (altitudeInt < rdr.getSha().getTargetAltitude())
      wrt.setTargetAltitude(altitudeInt);
  }

  private void flyRouteBehavior(FlyRouteBehavior behavior) {
    if (behavior.isApplied() == false) {
      wrt.setRouting(behavior.getCommands());
      behavior.setApplied();
    }
  }

  private void flyStage() {
    EAssert.isFalse(stages.isEmpty());
    ApproachStage stage = stages.getFirst();

    IApproachBehavior beh = stage.getBehavior();
    if (beh instanceof FlyRadialBehavior)
      flyRadialBehavior((FlyRadialBehavior) beh);
    else if (beh instanceof FlyRouteBehavior)
      flyRouteBehavior((FlyRouteBehavior) beh);
    else if (beh instanceof LandingBehavior)
      flyLandingBehavior((LandingBehavior) beh);
    else
      throw new EApplicationException("Unknown behavior type at this place.");

    if (isConditionTrue(stage.getExitCondition())) {
      // go to next stage
      setNextStage();
      return;
    }

    if (isConditionTrue(stage.getErrorCondition()))
      goAround(GoingAroundNotification.GoAroundReason.notStabilizedAirplane);

    updateApproachState();
  }

  private void flyLandingBehavior(LandingBehavior beh) {
    if (rdr.getState() != AirplaneState.landed) {
      int alt = rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude();
      if (alt < 20) {
        wrt.setState(AirplaneState.landed);
        wrt.setTargetAltitude(Context.getArea().getAirport().getAltitude());
      }
      if (alt < FLARE_HEIGHT) {
        wrt.setTargetAltitude(0);
      }
    }

    double hdg = RadialCalculator.getHeadingToFollowRadial(rdr.getCoordinate(),
            rdr.getRouting().getAssignedRunwayThreshold().getOtherThreshold().getCoordinate(),
            rdr.getRouting().getAssignedRunwayThreshold().getCourse(), 1);

    wrt.setTargetHeading(new HeadingNavigator(hdg, LeftRightAny.any));
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
            AirplaneState.approachEntry,
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
    this.stages.removeAt(0);
    EAssert.isFalse(this.stages.isEmpty());
    wrt.setState(AirplaneState.approachEntry);
  }
}
