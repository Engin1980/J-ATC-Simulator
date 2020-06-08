package eng.jAtcSim.newLib.area.traffic.fleets;


import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.area.airplanes.AirplaneType;
import eng.jAtcSim.newLib.area.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.world.xml.XmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetType {
  public static IList<FleetType> loadList(IReadOnlyList<XElement> type, AirplaneTypes airplaneTypes) {
    return null;
  }

  public static FleetType load(XElement source, AirplaneTypes airplaneTypes) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    int weight = XmlLoader.loadInteger("weight");

    AirplaneType at = airplaneTypes.tryGetByName(name);
    if (at == null)
      throw new EApplicationException(sf(
          "Airplane type defined in company fleets '%s' not found in known airplane types.", name));

    FleetType ret = new FleetType(at,weight);
    return ret;
  }


  private final int weight;
  private final AirplaneType airplaneType;

  public FleetType(AirplaneType airplaneType, int weight) {
    this.weight = weight;
    this.airplaneType = airplaneType;
  }

  public AirplaneType getAirplaneType() {
    return this.airplaneType;
  }

  public int getWeight() {
    return weight;
  }

  public String getName(){
    return this.airplaneType.name;
  }
}
