package eng.jAtcSim.abstractRadar.published;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.messaging.Participant;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class IMessage {
  private Participant sender;
  private Participant reciever;
  private String text;

  public IMessage(Participant sender, Participant reciever, String text) {
    this.sender = sender;
    this.reciever = reciever;
    this.text = text;
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
