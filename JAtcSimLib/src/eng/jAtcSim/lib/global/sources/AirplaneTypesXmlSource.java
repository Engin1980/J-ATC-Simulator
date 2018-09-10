package eng.jAtcSim.lib.global.sources;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class AirplaneTypesXmlSource extends XmlSource<AirplaneTypes> {

  public AirplaneTypesXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public AirplaneTypesXmlSource(){super(null);}

  @Override
  protected AirplaneTypes _load() {
    XmlSettings sett = new XmlSettings();

    XmlSerializer ser = new XmlSerializer(sett);

    AirplaneTypes ret = ser.deserialize(super.getXmlFileName(), AirplaneTypes.class);

    return ret;
  }

  public void init(){
    super.setInitialized();
  }
}
