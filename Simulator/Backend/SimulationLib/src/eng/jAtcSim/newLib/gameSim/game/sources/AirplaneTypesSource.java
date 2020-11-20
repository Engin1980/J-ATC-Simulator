package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplaneType.context.AirplaneTypeAcc;
import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.xml.airplaneTypes.AirplaneTypesXmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypesSource extends Source<AirplaneTypes> {

  private AirplaneTypes content;
  private final String fileName;


  public String getFileName() {
    return fileName;
  }

  AirplaneTypesSource(String xmlFile) {
    this.fileName = xmlFile;
  }

  @Override
  protected AirplaneTypes _getContent() {
    return content;
  }

  public void init() {
    try {
      this.content = AirplaneTypesXmlLoader.load(this.fileName);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load xml-airplaneTypes-file from '%s'", this.fileName), e);
    }

    IAirplaneTypeAcc airplaneTypeAcc = new AirplaneTypeAcc(this.content);
    ContextManager.setContext(IAirplaneTypeAcc.class, airplaneTypeAcc);

    super.setInitialized();
  }
}
