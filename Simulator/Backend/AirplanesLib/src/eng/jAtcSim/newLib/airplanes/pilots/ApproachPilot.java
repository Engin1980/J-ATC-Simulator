package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
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
import eng.jAtcSim.newLib.area.approaches.ApproachErrorCondition;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.*;
import eng.jAtcSim.newLib.area.approaches.conditions.FlyRouteBehaviorEmptyCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.RadialCalculator;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.EstablishedOnApproachNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;
import exml.annotations.XConstructor;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import java.util.Optional;

public class ApproachPilot extends Pilot {

  private static final int MAX_HEADING_DIFFERENCE = 90;
  private static final int FLARE_HEIGHT = 100;
  private static final int SHORT_FINAL_HEIGHT = 1000;
  private static final int SHORT_FINAL_DISTANCE = 3;
  private static final int LONG_FINAL_HEIGHT = 2000;
  private static final int LONG_FINAL_DISTANCE = 6;
  private static final int TOUCHDOWN_SIMULATED_HEIGHT = 20;

  private final IList<ApproachStage> stages;
  private final IList<ICommand> gaRouteCommands;
  private final ActiveRunwayThreshold threshold;
  private final int initialAltitude;
  private boolean switchToTowerRequested = false;
  /**
   * Represents plane height at the current second
   */
  private int height;

  @XConstructor
  private ApproachPilot(XLoadContext ctx) {
    super(ctx.getParents().get(Airplane.class));
    this.stages = null;
    this.gaRouteCommands = null;
    this.threshold = null;
    this.initialAltitude = 0;

    PostContracts.register(this, () -> stages != null, "Stages are null");
    PostContracts.register(this, () -> gaRouteCommands != null, "Ga-routes are null");
    PostContracts.register(this, () -> threshold != null, "Threshold is null");
  }

  public ApproachPilot(Airplane plane, Approach approach, ApproachEntry entry) {
    super(plane);
    EAssert.Argument.isNotNull(approach, "approach");
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isTrue(approach.getEntries().contains(entry));

    this.threshold = approach.getParent();
    this.stages = EList.of(approach.getStages());
    this.initialAltitude = approach.getInitialAltitude();

    this.stages.insertMany(0, entry.getEntryStages());

    if (approach.getBeforeStagesCommands().isEmpty() == false) {
      FlyRouteBehavior frb = new FlyRouteBehavior(approach.getBeforeStagesCommands().toList());
      ApproachStage iafStage = ApproachStage.create(
              "Initial approach commands",
              frb,
              new FlyRouteBehaviorEmptyCondition());
      this.stages.insert(0, iafStage);
    }

    this.gaRouteCommands = approach.getGaRoute().getRouteCommands().toList();

    EAssert.isTrue(this.stages.getLast().getBehavior() instanceof LandingBehavior, "The last approach stage should be with landing behavior.");
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
  public void xLoad(XElement elm, XLoadContext ctx) {
    super.xLoad(elm, ctx);

    ctx.fields.loadFieldItems(this, "stages", new EList<>(), ApproachStage.class, elm);
    ctx.fields.loadFieldItems(this, "gaRouteCommands", new EList<>(), ICommand.class, elm);
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    super.xSave(elm, ctx);
    ctx.fields.saveFieldItems(this, "stages", ApproachStage.class, elm);
    ctx.fields.saveFieldItems(this, "gaRouteCommands", ICommand.class, elm);
  }

  @Override
  protected void elapseSecondInternal() {
    this.height = rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude();
    flyStage();
  }

  private void flyRadialBehavior(FlyRadialBehavior behavior) {
    if (height > FLARE_HEIGHT && behavior instanceof FlyRadialWithDescentBehavior) {
      flyRadialWithDescentBehavior((FlyRadialWithDescentBehavior) behavior);
    }
    double heading = RadialCalculator.getHeadingToFollowRadial(
            rdr.getCoordinate(), behavior.getCoordinate(), behavior.getInboundRadialWithDeclination(),
            rdr.getSha().getSpeed(), MAX_HEADING_DIFFERENCE);

    heading = updateHeadingByWind(heading);

    wrt.setTargetHeading(new HeadingNavigator(heading, LeftRightAny.any));
  }

  private void flyToPointBehavior(FlyToPointBehavior behavior) {
    if (height > FLARE_HEIGHT && behavior instanceof FlyToPointWithDescentBehavior) {
      flyToPointWithDescentBehavior((FlyToPointWithDescentBehavior) behavior);
    }
    double heading = Coordinates.getBearing(rdr.getCoordinate(), behavior.getCoordinate());
    heading = updateHeadingByWind(heading);
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
        double dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), rdr.getRouting().getAssignedRunwayThreshold().getCoordinate());
        if (height < SHORT_FINAL_HEIGHT && dist < SHORT_FINAL_DISTANCE)
          newState = AirplaneState.shortFinal;
        else if (height < LONG_FINAL_HEIGHT && dist < LONG_FINAL_DISTANCE)
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

  private void flyToPointWithDescentBehavior(FlyToPointWithDescentBehavior behavior) {
    //TODO vyřešit identické s radialou
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
    else if (beh instanceof FlyToPointBehavior)
      flyToPointBehavior((FlyToPointBehavior) beh);
    else if (beh instanceof FlyRouteBehavior)
      flyRouteBehavior((FlyRouteBehavior) beh);
    else if (beh instanceof LandingBehavior)
      flyLandingBehavior((LandingBehavior) beh);
    else
      throw new EApplicationException("Unknown behavior type at this place.");

    if (isConditionTrue(stage.getExitCondition())) {
      setNextStage();
      return;
    }

    Optional<ApproachErrorCondition> err = stage.getErrorConditions().tryGetFirst(q -> isConditionTrue(q.getCondition()));
    if (err.isPresent()) {
      goAround(err.get().getGoAroundReason());
      return;
    }

    if (rdr.getAtc().getTunedAtc().getType() != AtcType.twr) {
      if (switchToTowerRequested == false && height < 1800) {
        wrt.sendMessage(new EstablishedOnApproachNotification(this.threshold.getName()));
        this.switchToTowerRequested = true;
      } else if (height < 500) {
        goAround(GoingAroundNotification.GoAroundReason.notOnTowerAtc);
        return;
      }
    }

    {
      Optional<GoingAroundNotification.GoAroundReason> ga = getReasonForGoAroundIfIsAfterThresholdOnLanding();
      if (ga.isPresent()){
        goAround(ga.get());
        return;
      }
    }

    updateApproachState();
  }

  private Optional<GoingAroundNotification.GoAroundReason> getReasonForGoAroundIfIsAfterThresholdOnLanding() {
    IApproachBehavior beh = stages.getFirst().getBehavior();
    if ((beh instanceof FlyRadialWithDescentBehavior || beh instanceof LandingBehavior) == false)
      // not on final approach
      return  Optional.empty();

    double crs = Coordinates.getBearing(rdr.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    double dcrs = Headings.getDifference(crs, this.getRunwayThreshold().getCourse(), true);
    if (dcrs < 90)
      // still before threshold
      return Optional.empty();

    double dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), this.getRunwayThreshold().getCoordinate());
    if (dist > 0.1)
      return Optional.of(GoingAroundNotification.GoAroundReason.unstabilizedHeading);

    double dalt = rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude();
    if (dalt > 100)
      return Optional.of(GoingAroundNotification.GoAroundReason.unstabilizedAltitude);

    return Optional.empty();
  }

  private void flyLandingBehavior(LandingBehavior beh) {

    if (isAfterRunwayThreshold()) {
      if (height > 130) {
        goAround(GoingAroundNotification.GoAroundReason.unstabilizedAltitude);
        return;
      }
      if (Coordinates.getDistanceToRadialInKm(rdr.getCoordinate(), threshold.getCoordinate(), threshold.getCourse()) > 0.1) {
        goAround(GoingAroundNotification.GoAroundReason.unstabilizedRadial);
        return;
      }
    }

    if (rdr.getState() != AirplaneState.landed) {
      if (height < TOUCHDOWN_SIMULATED_HEIGHT) {
        wrt.setState(AirplaneState.landed);
        wrt.setTargetAltitude(Context.getArea().getAirport().getAltitude());
      } else if (height < FLARE_HEIGHT) {
        wrt.setTargetAltitude(0);
      }
    }

    double hdg = RadialCalculator.getHeadingToFollowRadial(rdr.getCoordinate(),
            rdr.getRouting().getAssignedRunwayThreshold().getOtherThreshold().getCoordinate(),
            rdr.getRouting().getAssignedRunwayThreshold().getCourse(), rdr.getSha().getSpeed());

    hdg = updateHeadingByWind(hdg);

    wrt.setTargetHeading(new HeadingNavigator(hdg, LeftRightAny.any));
  }

  private double updateHeadingByWind(double hdg) {
    final double WIND_SPEED_CORRECTION_MULTIPLIER = 1 / 3d;
    int windHdg = Context.getWeather().getWeather().getWindHeading();
    int windSpd = Context.getWeather().getWeather().getWindSpeetInKts();
    double windDelta = Headings.getDifference(hdg, windHdg, false);
    boolean isLeft = windHdg < 180;
    double windEffect = (windDelta % 180) / 180d;
    windEffect = Math.sin(windEffect * Math.PI);
    double windCorrection = windEffect * windSpd * WIND_SPEED_CORRECTION_MULTIPLIER;
    if (isLeft)
      windCorrection *= -1;

    double ret = hdg + windCorrection;
    return ret;


    /*
    To test above behavior this can be used:

        int hdg = 220;

    System.out.println("Plane heading: " + hdg);

    int windSpeed = 8;

    for (int i = 0; i < 360; i+=10) {
      int windHdg = i;
      double hdgDelta = Headings.getDifference(hdg, windHdg, false);
      boolean isLeft = hdgDelta < 180;
      double windEffect = (hdgDelta % 180) / 180d;
      windEffect = Math.sin(windEffect * Math.PI);
      double windCorrection = windEffect * windSpeed / 3 * (isLeft ? -1 : 1);
      System.out.printf("%d : %.0f ( %.3f => %.0f ) - %s %n", windHdg, hdgDelta, windEffect, windCorrection, isLeft ? "left" : "right");
    }
     */
  }

  private boolean isAfterRunwayThreshold() {
    ActiveRunwayThreshold threshold = rdr.getRouting().getAssignedRunwayThreshold();
    double radialToThreshold = Coordinates.getBearing(rdr.getCoordinate(), threshold.getCoordinate());
    return Headings.isBetween(
            Headings.add(threshold.getOtherThreshold().getCourse(), -90),
            radialToThreshold,
            Headings.add(threshold.getOtherThreshold().getCourse(), 90));
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
