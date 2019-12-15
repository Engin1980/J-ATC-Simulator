package eng.jAtcSim.newLib.fleet;


import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class FleetType {

  public static FleetType load(XElement source) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    int weight = XmlLoader.loadInteger("weight");

    FleetType ret = new FleetType(name, weight);
    return ret;
  }

  private final int weight;
  private final String typeName;

  private FleetType(String typeName, int weight) {
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
