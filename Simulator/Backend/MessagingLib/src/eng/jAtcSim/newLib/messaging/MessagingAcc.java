package eng.jAtcSim.newLib.messaging;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class MessagingAcc {
  private Producer<Messenger> messengerProducer;

  public void setMessengerProducer(Producer<Messenger> messengerProducer) {
    EAssert.Argument.isNotNull(messengerProducer, "messengerProducer");
    this.messengerProducer = messengerProducer;
  }

  public Messenger getMessenger() {
    return messengerProducer.produce();
  }
}
