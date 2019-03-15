package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.world.approaches.IlsApproach;

public class XmlIlsApproach extends XmGuidedDescentApproach{

  public static class Category {

    public IlsApproach.IlsCategory type;
    public int daA;
    public int daB;
    public int daC;
    public int daD;

    public int getDA(char category) {
      int ret;
      switch (category) {
        case 'A':
          ret = this.daA;
          break;
        case 'B':
          ret = this.daB;
          break;
        case 'C':
          ret = this.daC;
          break;
        case 'D':
          ret = this.daD;
          break;
        default:
          throw new UnsupportedOperationException();
      }
      return ret;
    }
  }

  public enum Type {
    I,
    II,
    III
  }

  @XmlItemElement(elementName = "category", type = IlsApproach.Category.class)
  public IList<IlsApproach.Category> ilsCategories = new EList<>();
  @XmlOptional
  public Integer minimalInitialAltitude;
}
