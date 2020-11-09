package eng.jAtcSim.newLib.messaging.xml;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.Parser;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ParticipantParser implements Parser {

  @Override
  public Participant parse(String value) {
    Participant ret;
    String[] pts = value.split("::");
    EAssert.isTrue(pts.length == 2);

    switch (pts[0]){
      case "atc":
        ret = Participant.createAtc(pts[1]);
        break;
      case "airplane":
        ret = Participant.createAirplane(new Callsign(pts[1]));
        break;
      case "user":
        ret = Participant.createUser();
        break;
      case "system":
        ret = Participant.createSystem();
        break;
      default:
        throw new EEnumValueUnsupportedException(pts[0]);
    }

    return ret;
  }
}
