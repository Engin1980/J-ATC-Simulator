package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class ExpectedMovement {
  public ETimeStamp time;
  public boolean isArrival;
  public boolean isCommercial;
  public char category;

  public ExpectedMovement(ETimeStamp time, boolean isArrival, boolean isCommercial, char category) {
    this.time = time;
    this.isArrival = isArrival;
    this.isCommercial = isCommercial;
    this.category = category;
  }
}
