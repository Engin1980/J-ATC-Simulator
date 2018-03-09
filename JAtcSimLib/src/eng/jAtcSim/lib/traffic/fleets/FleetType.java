package eng.jAtcSim.lib.traffic.fleets;

import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class FleetType {
  public String name;
  public int weight;

  @XmlIgnore
  private AirplaneType referencedAirplaneType = null;

  public AirplaneType getAirplaneType() {
    if (referencedAirplaneType == null) {
      referencedAirplaneType = Acc.types().tryGetByName(this.name);
      if (referencedAirplaneType == null) {
        referencedAirplaneType = AirplaneTypes.getDefaultType();
      }
    }
    return this.referencedAirplaneType;
  }
}
