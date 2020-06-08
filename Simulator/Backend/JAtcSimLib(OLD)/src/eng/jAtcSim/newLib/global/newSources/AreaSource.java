package eng.jAtcSim.newLib.area.global.newSources;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.Area;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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
//    XmlSettings sett = new XmlSettings();
//
//    sett.forType(int.class)
//        .setCustomParser(new AltitudeValueParser());
//    sett.forType(Integer.class)
//        .setCustomParser(new AltitudeValueParser());
//    sett.forType(Coordinate.class)
//        .setCustomParser(new CoordinateValueParser());
//
//    XmlSerializer ser = new XmlSerializer(sett);
//
//    XmlArea xmlArea = ser.deserialize(this.fileName, XmlArea.class);
//    this.area = XmlModelBinder.convert(xmlArea);

    try {
      XDocument xDocument = XDocument.load(this.fileName);
      this.area = Area.load(xDocument.getRoot());
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Failed to load xml-area-file from '%s'", this.fileName), e);
    }

    super.setInitialized();
  }

  @Override
  protected Area _getContent() {
    return area;
  }
}
