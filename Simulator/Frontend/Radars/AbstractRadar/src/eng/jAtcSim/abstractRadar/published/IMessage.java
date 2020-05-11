package eng.jAtcSim.abstractRadar.published;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.messaging.Participant;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class IMessage {
  public enum eType{
    normal,
    rejection
  }
  private final Participant sender;
  private final Participant reciever;
  private final String text;
  private final eType type;

  public IMessage(Participant sender, Participant reciever, String text, eType type) {
    this.sender = sender;
    this.reciever = reciever;
    this.text = text;
    this.type = type;
  }

  public eType getType() {
    return type;
  }

  public Participant getSender() {
    return sender;
  }

  public Participant getReciever() {
    return reciever;
  }

  public String getText() {
    return text;
  }
}
