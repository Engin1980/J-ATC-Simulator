package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.jAtcSim.lib.world.approaches.UnpreciseApproach;

public class XmlUnpreciseApproach extends XmGuidedDescentApproach{
  public String faf;
  public String mapt;
  public int mdaA;
  public int mdaB;
  public int mdaC;
  public int mdaD;
  public UnpreciseApproach.Kind kind;

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
}
