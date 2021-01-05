package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.NonSimSharedAcc;
import eng.jAtcSim.newLib.xml.area.AreaXmlLoader;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaSource extends Source<Area> {

  private String fileName;
  private String icao;

  @XmlConstructor
  private AreaSource() {
  }

  AreaSource(String fileName, String icao) {
    this.fileName = fileName;
    this.icao = icao;
  }

  public Airport getActiveAirport() {
    Airport ret = getContent().getAirports().tryGetFirst(q -> q.getIcao().equals(icao));
    if (ret == null)
      throw new EApplicationException("Unable to load airport {" + icao + "} from selected area file.");
    return ret;
  }

  public Area getArea() {
    return getContent();
  }

  public String getFileName() {
    return fileName;
  }

  public String getIcao() {
    return icao;
  }

  public void setIcao(String icao) {
    this.icao = icao;
  }

  public void init() {
    try {
      Area area = AreaXmlLoader.load(this.fileName);
      super.setContent(area);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load xml-area-file from '%s'", this.fileName), e);
    }

    IAreaAcc areaAcc = new AreaAcc(this.getArea(), this.getActiveAirport());
    ContextManager.setContext(IAreaAcc.class, areaAcc);

    ISharedAcc sharedAcc = new NonSimSharedAcc(
            this.getActiveAirport().getIcao(),
            this.getActiveAirport().getAtcTemplates().select(q -> q.toAtcId()));
    ContextManager.setContext(ISharedAcc.class, sharedAcc);
  }
}
