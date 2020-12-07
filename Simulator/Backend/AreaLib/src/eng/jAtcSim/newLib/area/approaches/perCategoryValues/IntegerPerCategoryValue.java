package eng.jAtcSim.newLib.area.approaches.perCategoryValues;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.newXmlUtils.annotations.XmlConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class IntegerPerCategoryValue {

  private final int a;
  private final int b;
  private final int c;
  private final int d;

  public static IntegerPerCategoryValue create(int a, int b, int c, int d) {
    return new IntegerPerCategoryValue(a, b, c, d);
  }

  public static IntegerPerCategoryValue create(int value) {
    return new IntegerPerCategoryValue(value, value, value, value);
  }

  @XmlConstructor
  private IntegerPerCategoryValue(int a, int b, int c, int d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public int get(char category) {
    switch (category) {
      case 'a':
      case 'A':
        return this.a;
      case 'b':
      case 'B':
        return this.b;
      case 'c':
      case 'C':
        return this.c;
      case 'd':
      case 'D':
        return this.d;
      default:
        throw new EEnumValueUnsupportedException(category);
    }
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
