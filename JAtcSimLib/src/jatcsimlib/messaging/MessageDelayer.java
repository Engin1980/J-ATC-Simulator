package jatcsimlib.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MessageDelayer {

  class DelayedMessage {
    public final Message message;
    public int delayLeft;

    public DelayedMessage(Message message, int delay) {
      this.message = message;
      this.delayLeft = delay;
    }
  }

  private List<DelayedMessage> inner = new LinkedList<>();

  public void add (Message msg, int delay){
    int minDelay = getLastDelay() + delay; // todo here can be also "min" function
    DelayedMessage delayedMessage = new DelayedMessage(msg, minDelay);
    inner.add(delayedMessage);
  }

  public void add (Collection<Message> msgs, int delay){
    int minDelay = getLastDelay() + delay; // todo here can be also "min" function
    for (Message msg : msgs) {
      DelayedMessage delayedMessage = new DelayedMessage(msg, minDelay);
      inner.add(delayedMessage);
    }
  }

  public List<Message> get(){
    lowerDelay();
    List<Message> ret = new ArrayList<>();
    while (inner.isEmpty() == false){
      DelayedMessage delayedMessage = inner.get(0);
      if (delayedMessage.delayLeft > 0) break;

      ret.add(delayedMessage.message);
    }
    return ret;
  }

  private void lowerDelay() {
    for (DelayedMessage delayedMessage : inner) {
      delayedMessage.delayLeft = delayedMessage.delayLeft - 1;
    }
  }

  private int getLastDelay(){
    if (inner.isEmpty())
      return 0;
    else
      return inner.get(inner.size()-1).delayLeft;
  }

}
