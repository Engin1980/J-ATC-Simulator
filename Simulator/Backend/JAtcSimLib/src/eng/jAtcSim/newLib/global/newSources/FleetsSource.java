package eng.jAtcSim.newLib.area.global.newSources;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.area.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.traffic.fleets.Fleets;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetsSource extends Source<Fleets> {

  @XmlIgnore
  private Fleets content;
  private String fileName;

  public FleetsSource(String fileName) {
    this.fileName = fileName;
  }

  @Override
  protected Fleets _getContent() {
    return content;
  }

  public void init(AirplaneTypes types) {
    try {
      XDocument xDocument = XDocument.load(this.fileName);
      this.content = Fleets.load(xDocument.getRoot(), types);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Failed to load xml-fleets-file from '%s'", this.fileName), e);
    }

    super.setInitialized();
  }
}
