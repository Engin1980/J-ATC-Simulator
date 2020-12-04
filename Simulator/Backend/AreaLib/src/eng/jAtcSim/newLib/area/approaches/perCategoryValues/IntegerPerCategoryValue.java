package eng.jAtcSim.newLib.area.approaches.perCategoryValues;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class IntegerPerCategoryValue extends PerCategoryValue<Integer> {

  public static IntegerPerCategoryValue create(int a, int b, int c, int d) {
    return new IntegerPerCategoryValue(a, b, c, d);
  }

  public static IntegerPerCategoryValue create(int value) {
    return new IntegerPerCategoryValue(value, value, value, value);
  }

  private IntegerPerCategoryValue(int a, int b, int c, int d) {
    super(a, b, c, d);
  }

  @Override
  public String toString() {
    int a = this.get('a');
    int b = this.get('b');
    int c = this.get('c');
    int d = this.get('d');

    if (a != b || a != c || a != d)
      return sf("(%d/%d/%d/%d)", a, b, c, d);
    else
      return sf("%d", a);
  }
}
