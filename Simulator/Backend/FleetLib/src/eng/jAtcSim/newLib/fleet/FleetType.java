package eng.jAtcSim.newLib.fleet;


import eng.eSystem.validation.EAssert;
import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetType {

  public static FleetType create(String name, int weight) {
    return new FleetType(name, weight);
  }

  private final int weight;
  private final String typeName;

  private FleetType(String typeName, int weight) {
    EAssert.Argument.isNonemptyString(typeName, "typeName");
    EAssert.Argument.isTrue(weight >= 0, sf("Weight must be non-negative number (value=%d).", weight));
    this.weight = weight;
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public int getWeight() {
    return weight;
  }
}
