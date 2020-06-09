package eng.jAtcSim.newLib.messaging.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Messenger;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class MessagingAcc {
  private static Producer<Messenger> messengerProducer;

  public static void setMessengerProducer(Producer<Messenger> messengerProducer) {
    EAssert.Argument.isNotNull(messengerProducer, "messengerProducer");
    messengerProducer = messengerProducer;
  }

  public static Messenger getMessenger() {
    return messengerProducer.produce();
  }
}
