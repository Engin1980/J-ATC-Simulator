package eng.jAtcSim.newLib.airplanes.templates;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

public class ArrivalAirplaneTemplate extends AirplaneTemplate {
  private final Coordinate coordinate;
  private final int heading;
  private final int altitude;
  private final int speed;

  @XmlConstructor
  public ArrivalAirplaneTemplate() {
    super();

    coordinate = null;
    heading = -1;
    altitude = -1;
    speed = -1;

    PostContracts.register(this, () -> coordinate != null);
    PostContracts.register(this, () -> altitude != -1);
    PostContracts.register(this, () -> speed != -1);
    PostContracts.register(this, () -> heading != -1);
  }

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
}
