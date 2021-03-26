package eng.jAtcSim.layouting;

class Value {
  public enum Unit {
    pixel,
    percentage
  }

  public static Value createWild() {
    return new Value(Unit.pixel, null);
  }

  public static Value create(Unit unit, int value) {
    return new Value(unit, value);
  }

  public final Unit unit;
  public final Integer value;

  private Value(Unit unit, Integer value) {
    this.unit = unit;
    this.value = value;
  }

  public int convertValueToInt(int maximum) {
    if (value == null)
      return maximum;
    else if (unit == Unit.pixel)
      return value;
    else
      return (int) (maximum * value / 100d);
  }
}
