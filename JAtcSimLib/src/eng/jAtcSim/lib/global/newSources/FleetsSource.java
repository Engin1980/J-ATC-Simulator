package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;

public class FleetsSource extends Source<Fleets> {

  @XmlIgnore
  private Fleets fleets;
  private String xmlFileName;

  public FleetsSource(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  }

  @Override
  protected Fleets _getContent() {
    return null;
  }

  public void init(AirplaneTypes types) {
    XmlSettings sett = new XmlSettings();
    XmlSerializer ser = new XmlSerializer(sett);
    this.fleets = ser.deserialize(this.xmlFileName, Fleets.class);
    this.fleets.init(types);
    super.setInitialized();
  }
}
