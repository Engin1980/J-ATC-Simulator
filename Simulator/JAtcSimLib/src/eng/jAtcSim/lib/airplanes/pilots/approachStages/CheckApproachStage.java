package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;

public abstract class CheckApproachStage implements IApproachStage {

  public enum eResult{
    ok
  }

  public abstract eResult check(Airplane.Airplane4Command plane);
}
