package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class AirplaneTemplate {
  public Callsign callsign;
  public Coordinate coordinate;
  public AirplaneType airplaneType;
  public int heading;
  public int altitude;
  public int speed;
  public boolean isDeparture;
  public EntryExitPoint entryExitPoint;
  public EDayTimeStamp delayExpectedTime;

  public AirplaneTemplate(Callsign callsign, Coordinate coordinate, AirplaneType airplaneType,
                          int heading, int altitude, int speed, boolean isDeparture,
                          EntryExitPoint entryExitPoint, EDayTimeStamp appearanceTime) {
    this.callsign = callsign;
    this.coordinate = coordinate;
    this.airplaneType = airplaneType;
    this.heading = heading;
    this.altitude = altitude;
    this.speed = speed;
    this.isDeparture = isDeparture;
    this.entryExitPoint = entryExitPoint;
    this.delayExpectedTime = appearanceTime;
  }

}
