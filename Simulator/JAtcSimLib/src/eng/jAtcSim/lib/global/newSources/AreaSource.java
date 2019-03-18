package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.supports.IFactory;
import eng.jAtcSim.lib.coordinates.CoordinateValueParser;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.XmlModelBinder;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;
import eng.jAtcSim.lib.world.xmlModel.XmlArea;

public class AreaSource extends Source<Area> {

  @XmlIgnore
  private Area area;
  private String fileName;
  private String icao;

  public AreaSource(String fileName, String icao) {
    this.fileName = fileName;
    this.icao = icao;
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
    XmlSettings sett = new XmlSettings();

    sett.forType(int.class)
        .setCustomParser(new AltitudeValueParser());
    sett.forType(Integer.class)
        .setCustomParser(new AltitudeValueParser());
    sett.forType(Coordinate.class)
        .setCustomParser(new CoordinateValueParser());

    XmlSerializer ser = new XmlSerializer(sett);

    XmlArea xmlArea = ser.deserialize(this.fileName, XmlArea.class);
    this.area = XmlModelBinder.convert(xmlArea);

    super.setInitialized();
  }

  @Override
  protected Area _getContent() {
    return area;
  }
}
