package eng.jAtcSim.newLib.messaging.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSimLib.xmlUtils.Serializer;

public class ParticipantSerializer implements Serializer<Participant> {
  @Override
  public void invoke(XElement targetElement, Participant value) {
    targetElement.setContent(new ParticipantFormatter().invoke(value));
  }
}
