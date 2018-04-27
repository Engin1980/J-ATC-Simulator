package eng.jAtcSim.lib.global.sources;

import eng.eSystem.xmlSerialization.XmlListItemMapping;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.FleetType;
import eng.jAtcSim.lib.traffic.fleets.Fleets;

public class FleetsXmlSource extends XmlSource<Fleets> {

  public FleetsXmlSource(String xmlFile) {
    super(xmlFile);
  }

  @Override
  protected Fleets _load() {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("fleets$", CompanyFleet.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/company/types$", FleetType.class));

    XmlSerializer ser = new XmlSerializer(sett);
    Fleets ret = (Fleets) ser.deserialize(super.getXmlFileName(), Fleets.class);
    return ret;
  }

  public void init(AirplaneTypes types){
    super.setInitialized();
    this.getContent().init(types);
  }
}
