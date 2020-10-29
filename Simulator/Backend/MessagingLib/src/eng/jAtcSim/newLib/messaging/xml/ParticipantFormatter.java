package eng.jAtcSim.newLib.messaging.xml;

import eng.jAtcSim.newLib.messaging.Participant;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ParticipantFormatter implements  eng.jAtcSimLib.xmlUtils.Formatter<Participant> {
  @Override
  public String invoke(Participant participant) {
    return sf("%s::%s", participant.getType(), participant.getId());
  }
}
