package eng.jAtcSim.lib.messaging;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Messenger {

  public static class XSystem implements  IMessageParticipant {
    @Override
    public String getName() {
      return "SYSTEM";
    }

    public XSystem(){

    }

    @Override
    public String toString() {
      return "SYSTEM";
    }
  }

  public static final XSystem SYSTEM = new XSystem();
  private IList<Message> inner = new EList<>(LinkedList.class);
  @XmlIgnore
  private MessengerRecorder recorder = new MessengerRecorder("Messenger log", "messenger.log");

  public void send(Message msg) {
    synchronized (inner) {
      inner.add(msg);
      recorder.recordMessage(MessengerRecorder.eAction.ADD, msg);
    }
  }

  public IList<Message> getByTarget(IMessageParticipant participant, boolean deleteRetrieved) {
    IList<Message> ret;

    synchronized (inner) {
      ret = inner.where(q->q.getTarget() == participant);
      if (deleteRetrieved) {
        inner.remove(ret);
        ret.forEach(q->recorder.recordMessage(MessengerRecorder.eAction.GET,q ));
      }
    }

    return ret;

  }

}
