package eng.jAtcSim.lib.airplanes.behaviors;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.approachStagePilots.ApproachStagePilotProvider;
import eng.jAtcSim.lib.airplanes.approachStagePilots.IApproachStagePilot;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
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
  public void fly(IAirplaneWriteSimple plane) {
    if (this.currentStage == null) {
      startNextStage(plane);
    } else {
      switchToNextStageIfRequired(plane);
    }

    this.flyCurrentStage(plane);
  }

  private void switchToNextStageIfRequired(IAirplaneWriteSimple plane) {
    while (this.currentStagePilot.isFinishedStage(plane,this.currentStage)) {
      IApproachStagePilot.eResult result = this.currentStagePilot.disposeStage(plane, this.currentStage);
      if (result == IApproachStagePilot.eResult.ok) {
        approachInfo.getStages().removeAt(0);
        startNextStage(plane);
      } else {
        plane.getAdvanced().goAround(convertEResultToGaReason(result));
      }
    }
  }

  public NewApproachInfo getApproachInfo() {
    return approachInfo;
  }

  @Override
  public String toLogString() {
    return null;
  }

  private void flyCurrentStage(IAirplaneWriteSimple plane) {
    IApproachStagePilot.eResult result = this.currentStagePilot.flyStage(plane, this.currentStage);
    if (result != IApproachStagePilot.eResult.ok)
      plane.getAdvanced().goAround(convertEResultToGaReason(result));
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

  private void startNextStage(IAirplaneWriteSimple plane) {
    this.currentStage = this.approachInfo.getStages().get(0);
    this.currentStagePilot = ApproachStagePilotProvider.getPilot(this.currentStage);

    IApproachStagePilot.eResult result = this.currentStagePilot.initStage(plane, this.currentStage);
    if (result != IApproachStagePilot.eResult.ok)
      plane.getAdvanced().goAround(convertEResultToGaReason(result));
  }

}