package eng.jAtcSim.newLib.area.approaches.perCategoryValues;

public class DoublePerCategoryValue extends PerCategoryValue<Double> {
  public static DoublePerCategoryValue create(double value) {
    return new DoublePerCategoryValue(value,value,value,value);
  }

  private DoublePerCategoryValue(Double a, Double b, Double c, Double d) {
    super(a, b, c, d);
  }
}
