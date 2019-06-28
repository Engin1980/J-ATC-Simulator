package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public class NewApproachBehavior extends Behavior {

  private NewApproachInfo approachInfo;
  private int currentStageIndex = -1;

  public NewApproachBehavior(NewApproachInfo approachInfo) {
    Validator.isNotNull(approachInfo);
    this.approachInfo = approachInfo;
    Validator.check(this.approachInfo.getStages().isEmpty() == false,
        new EApplicationException("Created approach info has empty stages list."));
  }

  @Override
  public void fly(IPilot4Behavior pilot) {
    if (this.currentStageIndex == -1) {
      this.currentStageIndex++;
      startCurrentStage(pilot);
    } else {
      if (this.getCurrentStage().isFinishedStage(pilot)) {
        disposeCurrentStage(pilot);
        this.currentStageIndex++;
        startCurrentStage(pilot);
      }
    }

    this.flyCurrentStage(pilot);

  }

  @Override
  public String toLogString() {
    return null;
  }

  public NewApproachInfo getApproachInfo() {
    return this.approachInfo;
  }

  private void flyCurrentStage(IPilot4Behavior pilot) {
    this.getCurrentStage().flyStage(pilot);
  }

  private void disposeCurrentStage(IPilot4Behavior pilot) {
    this.getCurrentStage().disposeStage(pilot);
  }

  private IApproachStage getCurrentStage() {
    return this.approachInfo.getStages().get(this.currentStageIndex);
  }

  private void startCurrentStage(IPilot4Behavior pilot) {
    IApproachStage stage = this.approachInfo.getStages().get(this.currentStageIndex);
    stage.initStage(pilot);
  }

    public void goAround(IPilot4Behavior pilot, GoingAroundNotification.GoAroundReason reason) {
    assert reason != null;

    pilot.goAround(
        reason,
        this.approachInfo.getThreshold().getCourse(),
        this.approachInfo.getApproach().getGaCommands());


    boolean isAtcFail = EnumUtils.is(reason,
        new GoingAroundNotification.GoAroundReason[]{
            GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
            GoingAroundNotification.GoAroundReason.noLandingClearance,
            GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter,
            GoingAroundNotification.GoAroundReason.notStabilizedOnFinal
        });
    if (isAtcFail)
      pilot.experience(Mood.ArrivalExperience.goAroundNotCausedByPilot);




  }
}
