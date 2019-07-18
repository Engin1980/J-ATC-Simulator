package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.pilots.approachStagePilots.ApproachStagePilotProvider;
import eng.jAtcSim.lib.airplanes.pilots.approachStagePilots.IApproachStagePilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;

public class NewApproachBehavior extends Behavior {

  private NewApproachInfo approachInfo;
  private IApproachStagePilot currentStagePilot = null;
  private IApproachStage currentStage = null;

  public NewApproachBehavior(NewApproachInfo approachInfo) {
    Validator.isNotNull(approachInfo);
    Validator.check(this.approachInfo.getStages().isEmpty() == false,
        new EApplicationException("Created approach info has empty stages list."));
    this.approachInfo = approachInfo;
  }

  @Override
  public void fly(IPilotWriteSimple pilot) {
    if (this.currentStage == null) {
      startNextStage(pilot);
    } else {
      switchToNextStageIfRequired(pilot);
    }

    this.flyCurrentStage(pilot);
  }

  private void switchToNextStageIfRequired(IPilotWriteSimple pilot) {
    while (this.currentStagePilot.isFinishedStage(pilot,this.currentStage)) {
      IApproachStagePilot.eResult result = this.currentStagePilot.disposeStage(pilot, this.currentStage);
      if (result == IApproachStagePilot.eResult.ok) {
        approachInfo.getStages().removeAt(0);
        startNextStage(pilot);
      } else {
        pilot.getAdvanced().goAround(convertEResultToGaReason(result));
      }
    }
  }

  public NewApproachInfo getApproachInfo() {
    return approachInfo;
  }

  //TODO add following lines to go-around implementation if neccessary
//  public void goAround(IPilotWriteSimple pilot, GoingAroundNotification.GoAroundReason reason) {
//    assert reason != null;
//
//    pilot.goAround(
//        reason,
//        this.approachInfo.getThreshold().getCourse(),
//        this.approachInfo.getApproach().getGaCommands());
//
//
//    boolean isAtcFail = EnumUtils.is(reason,
//        new GoingAroundNotification.GoAroundReason[]{
//            GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
//            GoingAroundNotification.GoAroundReason.noLandingClearance,
//            GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter,
//            GoingAroundNotification.GoAroundReason.notStabilizedAirplane
//        });
//    if (isAtcFail)
//      pilot.getAdvanced().addExperience(Mood.ArrivalExperience.goAroundNotCausedByPilot);
//
//
//  }

  @Override
  public String toLogString() {
    return null;
  }

  private void flyCurrentStage(IPilotWriteSimple pilot) {
    IApproachStagePilot.eResult result = this.currentStagePilot.flyStage(pilot, this.currentStage);
    if (result != IApproachStagePilot.eResult.ok)
      pilot.getAdvanced().goAround(convertEResultToGaReason(result));
  }

  private GoingAroundNotification.GoAroundReason convertEResultToGaReason(IApproachStagePilot.eResult result) {
    switch(result){
      case runwayNotInSight:
        return GoingAroundNotification.GoAroundReason.runwayNotInSight;
      case altitudeTooHigh:
      case altitudeTooLow:
      case illegalLocation:
      case illegalHeading:
      case speedTooHigh:
      case speedTooLow:
        return GoingAroundNotification.GoAroundReason.notStabilizedAirplane;
      default:
        throw new EEnumValueUnsupportedException(result);
    }
  }

  private void startNextStage(IPilotWriteSimple pilot) {
    this.currentStage = this.approachInfo.getStages().get(0);
    this.currentStagePilot = ApproachStagePilotProvider.getPilot(this.currentStage);

    IApproachStagePilot.eResult result = this.currentStagePilot.initStage(pilot, this.currentStage);
    if (result != IApproachStagePilot.eResult.ok)
      pilot.getAdvanced().goAround(convertEResultToGaReason(result));
  }

}
