package eng.jAtcSim.newLib.shared;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class Restriction {

  public final AboveBelowExactly direction;
  public final int value;

  public Restriction(AboveBelowExactly direction, int value) {
    this.direction = direction;
    this.value = value;
  }

  @Override
  public String toString() {
    return direction + " " + value;
  }
  
}
