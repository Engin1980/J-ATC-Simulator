package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.ApproachInfo;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.IApproachStage;

public class NewApproachBehavior extends Behavior {

  ApproachInfo approachInfo;
  int currentStageIndex = -1;

  public NewApproachBehavior(ApproachInfo approachInfo) {
    Validator.isNotNull(approachInfo);
    this.approachInfo = approachInfo;
    Validator.check(this.approachInfo.getStages().isEmpty() == false,
        new EApplicationException("Created approach info has empty stages list."));
  }

  @Override
  public void fly(IPilot4Behavior pilot) {
    if (currentStageIndex == -1) {
      currentStageIndex++;
      startCurrentStage(pilot);
    } else {
      if (this.getCurrentStage().isFinishedStage(pilot)) {
        disposeCurrentStage(pilot);
        currentStageIndex++;
        startCurrentStage(pilot);
      }
    }

    this.flyCurrentStage(pilot);

  }

  private void flyCurrentStage(IPilot4Behavior pilot) {
    this.getCurrentStage().flyStage(pilot);
  }

  @Override
  public String toLogString() {
    return null;
  }

  private void disposeCurrentStage(IPilot4Behavior pilot) {
    this.getCurrentStage().disposeStage(pilot);
  }

  private IApproachStage getCurrentStage() {
    return this.approachInfo.getStages().get(currentStageIndex);
  }

  private void startCurrentStage(IPilot4Behavior pilot) {
    IApproachStage stage = approachInfo.getStages().get(currentStageIndex);
    stage.initStage(pilot);
  }
}
