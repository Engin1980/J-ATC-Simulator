package eng.jAtcSim.lib.traffic.fleets;


import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.Log;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

import static eng.jAtcSim.lib.global.logging.ApplicationLog.eType.warning;

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


  public void bindFleetType(AirplaneTypes types) {
    this.referencedAirplaneType = types.tryGetByName(name);
    if (referencedAirplaneType == null)
      Acc.log().writeLine(warning, "Airplane kind not found for %s", name);
  }
}
