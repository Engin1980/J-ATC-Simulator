package eng.jAtcSim.newLib.messaging.context;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Messenger;

public class MessagingAcc implements IMessagingAcc {
  private final Messenger messenger;

  public MessagingAcc(Messenger messenger) {
    EAssert.Argument.isNotNull(messenger, "messenger");
    this.messenger = messenger;
  }

  @Override
  public Messenger getMessenger() {
    return messenger;
  }
}
