package eng.jAtcSim.abstractRadar.support;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.abstractRadar.global.Point;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.gameSim.IAirplaneInfo;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.Squawk;

import java.util.LinkedList;
import java.util.List;

public class AirplaneDisplayInfo {
  private static final int DEFAULT_LABEL_SHIFT = 3;
  public final List<Coordinate> planeDotHistory = new LinkedList<>();
  public boolean wasUpdatedFlag = false;
  public Callsign callsign;
  public AirproxType airprox;
  public boolean mrvaError;
  public Coordinate coordinate;
  public int heading;
  public int ias;
  public int tas;
  public int verticalSpeed;
  public AtcId tunedAtc;
  public AtcId responsibleAtc;
  public boolean hasRadarContact;
  public AirplaneType type;
  public Point labelShift;
  public boolean fixedLabelShift = false;
  public boolean isConfirmedSwitch;
  public Squawk squawk;
  public int targetHeading;
  public int targetSpeed;
  public int altitude;
  public int targetAltitude;
  public boolean emergency;


  public AirplaneDisplayInfo(IAirplaneInfo planeInfo) {
    this.callsign = planeInfo.callsign();
    this.type = planeInfo.planeType();
    this.squawk = planeInfo.squawk();
    this.setDefaultLabelPosition();
  }

  public String format(String pattern) {
    StringBuilder sb = new StringBuilder(pattern);
    int[] p = new int[2];

    while (true) {
      updatePair(sb, p);
      if (p[0] < 0) {
        break;
      }

      String tmp = sb.substring(p[0] + 1, p[1]);
      int index = Integer.parseInt(tmp);
      sb.replace(p[0], p[1] + 1, getFormatValueByIndex(index));
    }

    return sb.toString();
  }

  public void setDefaultLabelPosition() {
    this.labelShift = new Point(DEFAULT_LABEL_SHIFT, DEFAULT_LABEL_SHIFT);
  }

  public void updateInfo(IAirplaneInfo plane) {
    wasUpdatedFlag = true;

    this.ias = plane.ias();
    this.tas = (int) plane.tas();
    this.targetSpeed = (int) plane.targetSpeed();
    this.altitude = plane.altitude();
    this.targetAltitude = plane.targetAltitude();
    this.verticalSpeed = plane.verticalSpeed();
    this.heading = plane.heading();
    this.targetHeading = plane.targetHeading();

    this.tunedAtc = plane.tunedAtc();
    this.responsibleAtc = plane.responsibleAtc();
    this.hasRadarContact = plane.hasRadarContact();

    this.airprox = plane.getAirprox();
    this.mrvaError = plane.isMrvaError();
    this.emergency = plane.isEmergency();

    this.coordinate = plane.coordinate();

    this.isConfirmedSwitch = plane.isUnderConfirmedSwitch();

    planeDotHistory.add(plane.coordinate());
  }

  private String getFormatValueByIndex(int index) {
    switch (index) {
      case 1:
        if (this.emergency)
          return this.callsign.toString() + " !";
        else
          return this.callsign.toString();
      case 2:
        return this.callsign.getCompany();
      case 3:
        return this.callsign.getNumber();
      case 4:
        return this.type.name;
      case 5:
        return Character.toString(this.type.category);
      case 8:
        return this.squawk.toString();
      case 11:
        return Format.Heading.to(this.heading);
      case 12:
        return Format.Heading.toShort(this.heading);
      case 15:
        return Format.Heading.to(this.targetHeading);
      case 16:
        return Format.Heading.toShort(this.targetHeading);
      case 21:
        return Format.Speed.toLong(this.tas);
      case 22:
        return Format.Speed.toShort(this.tas);
      case 23:
        return Format.Speed.toRightAligned(this.tas);
      case 31:
        return Format.Speed.toLong(this.targetSpeed);
      case 32:
        return Format.Speed.toShort(this.targetSpeed);
      case 33:
        return Format.Altitude.toFLLong(this.altitude);
      case 34:
        return Format.Altitude.toFLShort(this.altitude);
      case 35:
        return Format.Altitude.toAlfOrFLLong(this.altitude);
      case 36:
        return Format.Altitude.toFLLong(this.targetAltitude);
      case 37:
        return Format.Altitude.toFLShort(this.targetAltitude);
      case 38:
        return Format.Altitude.toAlfOrFLLong(this.targetAltitude);
      case 41:
        return Format.VerticalSpeed.formatVerticalSpeedLong(this.verticalSpeed);
      case 42:
        return Format.VerticalSpeed.formatVerticalSpeedShort(this.verticalSpeed);
      case 43:
        return Format.VerticalSpeed.getClimbDescendChar(this.verticalSpeed);
      default:
        return "???";
    }
  }

  private void updatePair(StringBuilder ret, int[] p) {
    int start = ret.indexOf("{");
    if (start < 0) {
      p[0] = -1;
      return;
    }
    p[0] = start;
    int end = ret.indexOf("}", start);
    p[1] = end;
  }

}
