package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.xml.airplaneTypes.AirplaneTypesXmlLoader;

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
      this.content = AirplaneTypesXmlLoader.load(this.fileName);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load xml-airplaneTypes-file from '%s'", this.fileName), e);
    }
    super.setInitialized();
  }
}
