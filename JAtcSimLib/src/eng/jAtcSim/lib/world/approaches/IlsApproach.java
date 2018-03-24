package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.KeyList;

public class IlsApproach extends Approach {

  public static class Category implements KeyItem<Type> {

    private Type type;
    private int daA;
    private int daB;
    private int daC;
    private int daD;

    @Override
    public Type getKey() {
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

    public Category() {
    }
  }

  public enum Type {
    I,
    II,
    III
  }

  private KeyList<Category, Type> categories = new KeyList<IlsApproach.Category, Type>();

  public KeyList<Category, Type> getCategories() {
    return categories;
  }

  @Override
  protected void _bind() {

  }


}
