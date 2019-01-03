package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class AirplaneTypesSource extends Source<AirplaneTypes> {

  @XmlIgnore
  private AirplaneTypes content;
  private String fileName;

  public AirplaneTypesSource(String xmlFile) {
    this.fileName = xmlFile;
  }

  @Override
  protected AirplaneTypes _getContent() {
    return content;
  }

  public void init() {
    XmlSettings sett = new XmlSettings();
    XmlSerializer ser = new XmlSerializer(sett);
    content = ser.deserialize(this.fileName, AirplaneTypes.class);
    super.setInitialized();
  }
}
