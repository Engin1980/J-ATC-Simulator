package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;

public class IlsApproach extends Approach {

  public static class Category {

    private IlsCategory type;
    private int daA;
    private int daB;
    private int daC;
    private int daD;

    public Category() {
    }

    public IlsCategory getType() {
      return type;
    }

    public int getDaA() {
      return daA;
    }

    public int getDaB() {
      return daB;
    }

    public int getDaC() {
      return daC;
    }

    public int getDaD() {
      return daD;
    }

    public int getDA(char category) {
      int ret;
      switch (category) {
        case 'A':
          ret = this.getDaA();
          break;
        case 'B':
          ret = this.getDaB();
          break;
        case 'C':
          ret = this.getDaC();
          break;
        case 'D':
          ret = this.getDaD();
          break;
        default:
          throw new UnsupportedOperationException();

      }
      return ret;
    }
  }

  public enum IlsCategory {
    I,
    II,
    III
  }

  @XmlItemElement(elementName = "category", type = Category.class)
  private IList<Category> categories = new EList<>();
  @XmlOptional
  private double glidePathPercentage = 3;

  public IReadOnlyList<Category> getCategories() {
    return categories;
  }

  public double getGlidePathPercentage() {
    return glidePathPercentage;
  }

  @Override
  public String getTypeString() {
    return "ILS";
  }

  @Override
  protected void _bind() {

  }


}
