package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class ChangeAltitudeCommand implements IAtcCommand {

  public enum eDirection {
    any,
    climb,
    descend
  }

  public static ChangeAltitudeCommand create(eDirection direction, int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(direction, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand create(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.any, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createClimb(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.climb, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createDescend(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.descend, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand load(XElement source) {
    assert source.getName().equals("altitude");

    XmlLoader.setContext(source);
    String dirS = XmlLoader.loadString("direction", "set");
    eDirection dir;
    if (dirS.equals("set"))
      dir = eDirection.any;
    else
      dir = Enum.valueOf(eDirection.class, dirS);
    int alt = XmlLoader.loadAltitude("value");
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(dir, alt);
    return ret;
  }

  private final eDirection direction;
  private final int altitudeInFt;

  private ChangeAltitudeCommand(eDirection direction, int altitudeInFt) {
    this.direction = direction;
    this.altitudeInFt = altitudeInFt;
  }

  public int getAltitudeInFt() {
    return altitudeInFt;
  }

  public eDirection getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return "Altitude change to " + altitudeInFt + " {command}";
  }
}
