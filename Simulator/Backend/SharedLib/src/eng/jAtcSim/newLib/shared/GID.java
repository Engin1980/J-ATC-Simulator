package eng.jAtcSim.newLib.shared;

import eng.eSystem.validation.EAssert;

public class GID {
  private static int lastId = 0;

  public static GID create() {
    lastId++;
    GID ret = new GID(lastId);
    return ret;
  }

  public static GID create(int value) {
    EAssert.Argument.isTrue(value > lastId);
    lastId = value;
    return new GID(value);
  }

  public final int value;

  private GID(int value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "GID{" + value + '}';
  }
}
