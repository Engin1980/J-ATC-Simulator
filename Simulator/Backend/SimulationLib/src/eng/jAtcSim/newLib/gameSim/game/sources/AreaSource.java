package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.NonSimSharedAcc;
import eng.jAtcSim.newLib.xml.area.AreaXmlLoader;

import exml.IXPersistable;
import exml.annotations.XConstructor;

import java.util.Optional;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaSource extends Source<Area> {

  private String fileName;
  private String icao;

  @XConstructor

  private AreaSource() {
  }

  AreaSource(String fileName, String icao) {
    this.fileName = fileName;
    this.icao = icao;
  }

  public Airport getActiveAirport() {
    Optional<Airport> ret = getContent().getAirports().tryGetFirst(q -> q.getIcao().equals(icao));
    if (ret.isEmpty())
      throw new ApplicationException("Unable to load airport {" + icao + "} from selected area file.");
    return ret.get();
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
      throw new ApplicationException(sf("Failed to load xml-area-file from '%s'", this.fileName), e);
    }

    IAreaAcc areaAcc = new AreaAcc(this.getArea(), this.getActiveAirport());
    ContextManager.setContext(IAreaAcc.class, areaAcc);

    ISharedAcc sharedAcc = new NonSimSharedAcc(
            this.getActiveAirport().getIcao(),
            this.getActiveAirport().getAtcTemplates().select(q -> q.toAtcId()));
    ContextManager.setContext(ISharedAcc.class, sharedAcc);
  }
}
