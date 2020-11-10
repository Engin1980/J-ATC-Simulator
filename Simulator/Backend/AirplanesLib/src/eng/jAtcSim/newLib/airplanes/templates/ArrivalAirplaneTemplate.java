package eng.jAtcSim.newLib.airplanes.templates;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class ArrivalAirplaneTemplate extends AirplaneTemplate {
  private final Coordinate coordinate;
  private final int heading;
  private final int altitude;
  private final int speed;

  public ArrivalAirplaneTemplate(Callsign callsign, AirplaneType airplaneType, EntryExitPoint entryExitPoint,
                                 EDayTimeStamp entryTime, int entryDelay, EDayTimeStamp expectedExitTime,
                                 Coordinate coordinate,
                                 int heading, int altitude, int speed) {
    super(callsign, airplaneType, entryExitPoint, expectedExitTime, entryTime, entryDelay);
    this.coordinate = coordinate;
    this.heading = heading;
    this.altitude = altitude;
    this.speed = speed;
  }

  public int getAltitude() {
    return altitude;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public EntryExitPoint getEntryPoint() {
    return super.entryExitPoint;
  }

  public int getHeading() {
    return heading;
  }

  public int getSpeed() {
    return speed;
  }

  @Override
  protected void _save(XElement target) {
    XmlSaveUtils.Field.storeFields(target, this, "heading", "altitude", "speed");
    XmlSaveUtils.Field.storeField(target, this, "coordinate", SharedXmlUtils.Formatters.coordinateFormatter);
  }
}
