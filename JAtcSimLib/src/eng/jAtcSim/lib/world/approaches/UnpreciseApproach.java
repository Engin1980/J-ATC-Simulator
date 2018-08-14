package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinate;
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
   * Name of mapt where course is relative to.
   */
  private String mapt;
  private Navaid _mapt;
  private int mdaA;
  private int mdaB;
  private int mdaC;
  private int mdaD;
  private Type type;

  public Type getType() {
    return type;
  }

  public Navaid getFaf() {
    return _faf;
  }

  public Coordinate getMAPt() {
    return _mapt.getCoordinate();
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
  public String getTypeString() {
    switch (this.type){
      case ndb:
        return "NDB";
      case vor:
        return "VOR";
      default:
        throw new EEnumValueUnsupportedException(this.type);
    }
  }

  @Override
  protected void _bind() {
    _faf = Acc.area().getNavaids().getOrGenerate(faf);
    _mapt = Acc.area().getNavaids().getOrGenerate(mapt);
  }
}
