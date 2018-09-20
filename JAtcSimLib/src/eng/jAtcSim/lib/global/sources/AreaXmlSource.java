package eng.jAtcSim.lib.global.sources;

import eng.eSystem.xmlSerialization.*;
import eng.eSystem.xmlSerialization.supports.IFactory;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

@Deprecated
public class AreaXmlSource extends XmlSource<Area> {

  private int activeAirportIndex = 0;

  public AreaXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public AreaXmlSource() {
    super(null);
  }

  public int getActiveAirportIndex() {
    return activeAirportIndex;
  }

  public Airport getActiveAirport() {
    Airport ret = super.getContent().getAirports().get(this.activeAirportIndex);
    return ret;
  }

  public void setActiveAirport(String icao) {
    this.activeAirportIndex =
        super.getContent().getAirports().getIndexOf(q -> q.getIcao().equals(icao));
  }

  public void init(String icao) {
    super.setInitialized();
    super.getContent().init();
    this.setActiveAirport(icao);
  }

  public void init(int index) {
    super.setInitialized();
    super.getContent().init();
    this.setActiveAirport(super.getContent().getAirports().get(index).getIcao());
  }

  @Override
  protected Area _load() {
    XmlSettings sett = new XmlSettings();

    sett.forType(Area.class).setFactory(new AreaFactory());
    sett.forType(int.class)
        .setCustomParser(new AltitudeValueParser());
    sett.forType(Integer.class)
        .setCustomParser(new AltitudeValueParser());

    XmlSerializer ser = new XmlSerializer(sett);

    Area ret = ser.deserialize(super.getXmlFileName(), Area.class);
    return ret;
  }
}

@Deprecated
class AreaFactory implements IFactory<Area> {

  @Override
  public Area createInstance() {
    Area ret = Area.create();
    return ret;
  }
}