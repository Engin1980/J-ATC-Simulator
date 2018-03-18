package eng.jAtcSim.lib.speaking.fromAtc.atc2atc;

import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.world.Runway;

public class RunwayCheck implements IAtc2Atc {

  public enum eType{
    askForTime,
    doCheck
  }

  public Runway runway;
  public eType type;

  public RunwayCheck(Runway runway, eType type) {
    this.runway = runway;
    this.type = type;
  }
}
