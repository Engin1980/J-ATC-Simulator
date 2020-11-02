package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.xml.area.AreaXmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaSource extends Source<Area> {

  private Area area;
  private String fileName;
  private String icao;

  AreaSource(String fileName, String icao) {
    this.fileName = fileName;
    this.icao = icao;
  }

  public Area getArea() {
    return area;
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

  public Airport getActiveAirport() {
    Airport ret = area.getAirports().tryGetFirst(q -> q.getIcao().equals(icao));
    if (ret == null)
      throw new EApplicationException("Unable to load airport {" + icao + "} from selected area file.");
    return ret;
  }

  public void init() {
    try {
      this.area = AreaXmlLoader.load(this.fileName);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load xml-area-file from '%s'", this.fileName), e);
    }

    super.setInitialized();
  }

  @Override
  protected Area _getContent() {
    return area;
  }
}
