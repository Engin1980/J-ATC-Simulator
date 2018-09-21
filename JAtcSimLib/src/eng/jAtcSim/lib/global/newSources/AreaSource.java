package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.supports.IFactory;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

public class AreaSource extends Source<Area> {

  private static class AreaFactory implements IFactory<Area> {

    @Override
    public Area createInstance() {
      Area ret = Area.create();
      return ret;
    }
  }

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

  public Airport getActiveAirport(){
    Airport ret = area.getAirports().getFirst(q->q.getIcao().equals(icao));
    return ret;
  }

  @Override
  protected Area _getContent() {
    return area;
  }

  public void init() {
    XmlSettings sett = new XmlSettings();

    sett.forType(Area.class).setFactory(new AreaFactory());
    sett.forType(int.class)
        .setCustomParser(new AltitudeValueParser());
    sett.forType(Integer.class)
        .setCustomParser(new AltitudeValueParser());

    XmlSerializer ser = new XmlSerializer(sett);

    this.area = ser.deserialize(this.fileName, Area.class);

    this.area.init();

    super.setInitialized();
  }
}
