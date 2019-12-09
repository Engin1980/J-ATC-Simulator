package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.sharedLib.exceptions.ApplicationException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class ChangeSpeedCommand implements IAtcCommand {

  public enum eRestriction{
    below,
    above,
    exactly
  }

  public static IAtcCommand load(XElement element) {
    assert element.getName().equals("speed");
    XmlLoader.setContext(element);
    /*
      <xs:simpleType name="RestrictionType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="below"/>
      <xs:enumeration value="above"/>
      <xs:enumeration value="exactly"/>
      <xs:enumeration value="clear"/>
    </xs:restriction>
  </xs:simpleType>
     */
    String restriction = XmlLoader.loadString("restriction");
    int speed = XmlLoader.loadInteger("value");

    ChangeSpeedCommand ret = new ChangeSpeedCommand();
  }

  private final eRestriction restriction;
  private final Integer value;

  public static ChangeSpeedCommand create(eRestriction direction, int speedInKts) {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(direction, speedInKts);
    return ret;
  }

  public static ChangeSpeedCommand createResumeOwnSpeed() {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(eRestriction.exactly, null);
    return ret;
  }

  private ChangeSpeedCommand(eRestriction restriction, Integer value) {
    this.restriction = restriction;
    this.value = value;
  }

  public eRestriction getDirection() {
    if (isResumeOwnSpeed())
      throw new ApplicationException("Unable to call this on 'ResumeOwnSpeed' command.");
    return this.restriction;
  }

  public int getSpeedInKts() {
    if (isResumeOwnSpeed())
      throw new ApplicationException("Unable to call this on 'ResumeOwnSpeed' command.");
    return this.value;
  }

  public boolean isResumeOwnSpeed() {
    return this.value == null;
  }

  @Override
  public String toString() {
    if (isResumeOwnSpeed()) {
      return "Resume own speed {command}";
    } else {
      switch (this.restriction) {
        case above:
          return "Speed at least " + this.value + "kts {command}";
        case below:
          return "Speed at most " + this.value + "kts {command}";
        case exactly:
          return "Speed exactly " + this.value + "kts {command}";
        default:
          throw new EEnumValueUnsupportedException(this.restriction);
      }
    }
  }
}
