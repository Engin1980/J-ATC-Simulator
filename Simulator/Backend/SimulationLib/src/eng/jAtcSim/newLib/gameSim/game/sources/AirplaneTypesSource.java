package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplaneType.context.AirplaneTypeAcc;
import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.xml.airplaneTypes.AirplaneTypesXmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypesSource extends Source<AirplaneTypes> {

  private final String fileName;

  public String getFileName() {
    return fileName;
  }

  AirplaneTypesSource(String xmlFile) {
    this.fileName = xmlFile;
  }

  public void init() {
    try {
      AirplaneTypes at = AirplaneTypesXmlLoader.load(this.fileName);
      super.setContent(at);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load xml-airplaneTypes-file from '%s'", this.fileName), e);
    }

    IAirplaneTypeAcc airplaneTypeAcc = new AirplaneTypeAcc(super.getContent());
    ContextManager.setContext(IAirplaneTypeAcc.class, airplaneTypeAcc);
  }
}
