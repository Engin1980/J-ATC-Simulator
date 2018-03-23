package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.world.Navaid;

public class UnpreciseApproach extends Approach {

  public enum Type {
    vor,
    ndb
  }

  /**
   * Name of FAF navaid, where descend begins.
   */
  private String faf;
  private Navaid _faf;
  /**
   * Name of fix where course is relative to.
   */
  private String fix;
  private Navaid _navaid;
  private int mdaA;
  private int mdaB;
  private int mdaC;
  private int mdaD;
  private Type type;

  public Type getType() {
    return type;
  }

  public int getMDA(char category) {
    switch (category) {
      case 'A':
        return mdaA;
      case 'B':
        return mdaB;
      case 'C':
        return mdaC;
      case 'D':
        return mdaD;
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  protected void _bind() {
    _faf = Acc.area().getNavaids().get(faf);
    _navaid = Acc.area().getNavaids().get(fix);
  }
}
