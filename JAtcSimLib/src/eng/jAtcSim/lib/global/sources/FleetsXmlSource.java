package eng.jAtcSim.lib.global.sources;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;

public class FleetsXmlSource extends XmlSource<Fleets> {

  public FleetsXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public FleetsXmlSource(){super(null);}

  @Override
  protected Fleets _load() {
    XmlSettings sett = new XmlSettings();
    XmlSerializer ser = new XmlSerializer(sett);

    Fleets ret = ser.deserialize(super.getXmlFileName(), Fleets.class);

    return ret;
  }

  public void init(AirplaneTypes types){
    super.setInitialized();
    this.getContent().init(types);
  }
}
