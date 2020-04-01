package eng.jAtcSim.newLib.area.approaches.perCategoryValues;

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
}
