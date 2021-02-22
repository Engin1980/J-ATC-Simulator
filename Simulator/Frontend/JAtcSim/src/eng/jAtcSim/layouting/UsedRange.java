package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class UsedRange {

  private static <T> T as(Object instance, Class<? extends T> type) {
    return (T) instance;
  }

  private final int maximum;
  private int usedPixels;
  private int usedPercents;
  private int wildCount;

  public UsedRange(int maximum) {
    EAssert.Argument.isTrue(maximum > 0);
    this.maximum = maximum;
  }

  public int getPercentageWidth(int value) {
    return (int) (maximum * value / 100d);
  }

  public int getPercentageWidth(PercentageValue value) {
    return getPercentageWidth(value.getValue());
  }

  public int getWidth(Value value) {
    int ret;
    if (value instanceof PixelValue)
      ret = as(value, PixelValue.class).getValue();
    else if (value instanceof PercentageValue)
      ret = getPercentageWidth((PercentageValue) value);
    else if (value instanceof WildValue)
      ret = getWildWidth();
    else
      throw new UnsupportedOperationException();

    return ret;
  }

  public int getWildWidth() {
    int widthLeft = getWidthLeft();
    return widthLeft / Math.max(wildCount, 1);
  }

  public void use(Iterable<Value> values) {
    for (Value value : values) {
      if (value instanceof PixelValue)
        usedPixels += as(value, PixelValue.class).getValue();
      else if (value instanceof PercentageValue)
        usedPercents += as(value, PercentageValue.class).getValue();
      else if (value instanceof WildValue)
        wildCount++;
    }
  }

  private int getWidthLeft() {
    int used = 0;
    used += usedPixels;
    used += (int) ((usedPercents / 100d) * maximum);
    return maximum - used;
  }
}
