package eng.jAtcSim.newLib.airplanes.approachStagePilots;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.world.approaches.stages.*;

public class ApproachStagePilotProvider {

  private static IMap<Class, IApproachStagePilot> inner = new EMap<>();

  public static IApproachStagePilot getPilot(IApproachStage stage) {
    Validator.isNotNull(stage);

    IApproachStagePilot ret;
    if (stage instanceof ICheckStage)
      ret = getInstance(CheckStagePilot.class);
    else if (stage instanceof FlyToPointStage)
      ret = getInstance(FlyToPointStagePilot.class);
    else if (stage instanceof LandingStage)
      ret = getInstance(LandingStagePilot.class);
    else if (stage instanceof RadialStage)
      ret = getInstance(RadialStagePilot.class);
    else if (stage instanceof RouteStage)
      ret = getInstance(RouteStagePilot.class);
    else
      throw new UnsupportedOperationException("Unknown stage type of '" + stage.getClass().getSimpleName() + "'.");

    return ret;
  }

  private static IApproachStagePilot getInstance(Class<? extends IApproachStagePilot> stageType) {
    IApproachStagePilot ret = inner.tryGet(stageType);
    if (ret == null) {
      try {
        ret = stageType.newInstance();
      } catch (Exception ex) {
        throw new EApplicationException("Failed to create a new instance of a pilot-stage '" + stageType.getSimpleName() + "'.", ex);
      }
      inner.set(stageType, ret);
    }
    return ret;
  }
}
