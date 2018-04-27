package eng.jAtcSim.lib.global.sources;

import eng.eSystem.xmlSerialization.XmlListItemMapping;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class AirplaneTypesXmlSource extends XmlSource<AirplaneTypes> {

  public AirplaneTypesXmlSource(String xmlFile) {
    super(xmlFile);
  }

  @Override
  protected AirplaneTypes _load() {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");

    // mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("planeTypes$", AirplaneType.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());

    XmlSerializer ser = new XmlSerializer(sett);
    AirplaneTypes ret = (AirplaneTypes) ser.deserialize(super.getXmlFileName(), AirplaneTypes.class);
    return ret;
  }

  public void init(){
    super.setInitialized();
  }
}
