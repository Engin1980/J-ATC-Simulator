package eng.jAtcSim.newLib.area.global.newSources;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.newLib.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.area.airplanes.AirplaneTypes;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypesSource extends Source<AirplaneTypes> {

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
//    XmlSettings sett = new XmlSettings();
//    XmlSerializer ser = new XmlSerializer(sett);
//    content = ser.deserialize(this.fileName, AirplaneTypes.class);
//    super.setInitialized();

    try {
      XDocument xDocument = XDocument.load(this.fileName);
      this.content = AirplaneTypes.load(xDocument.getRoot());
    } catch (EXmlException e) {
      throw new ApplicationException(sf("Failed to load xml-airplaneTypes-file from '%s'", this.fileName), e);
    }
  }
}
