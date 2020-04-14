package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRouteBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.area.routes.IafRoute;

import java.util.concurrent.ExecutionException;

public class ApproachPilot extends Pilot {

  private final Approach approach;
  private final IafRoute iafRoute;
  private Integer currentStageIndex = null;

  public ApproachPilot(IPilotPlane plane,
                       Approach approach, ApproachEntry entry) {
    super(plane);
    EAssert.Argument.isNotNull(approach, "approach");
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isTrue(approach.getEntries().contains(entry));
    EAssert.Argument.isTrue(entry.getEntryLocation().isInside(plane.getCoordinate()));

    this.approach = approach;
    this.iafRoute = entry.getIafRoute() == null ? null : entry.getIafRoute().createClone();
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
          currentStageIndex = 0;
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
    else if (beh instanceof FlyRouteBehavior)
      flyRouteBehavior((FlyRouteBehavior) beh);
    else
      throw new EApplicationException("Unknown behavior type at this place.");

    ICondition it = getConditionTrue(stage.getErrorCondition());
    if (it != null)
      goAround(it);
  }

  private boolean isConditionTrue(ICondition condition) {
    return getConditionTrue(condition) != null;
  }

  private ICondition getConditionTrue(ICondition condition) {
return condition which is true
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
}
