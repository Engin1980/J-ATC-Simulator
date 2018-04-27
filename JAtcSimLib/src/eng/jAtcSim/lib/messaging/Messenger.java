package eng.jAtcSim.lib.messaging;

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
  private List<Message> inner = new LinkedList<>();
  @XmlIgnore
  private MessengerRecorder recorder = new MessengerRecorder("Messenger log", "messenger.log");

  public void send(Message msg) {
    synchronized (inner) {
      inner.add(msg);
      recorder.recordMessage(MessengerRecorder.eAction.ADD, msg);
    }
  }

  public List<Message> getByTarget(IMessageParticipant participant, boolean deleteRetrieved) {
    List<Message> ret;
    synchronized (inner) {
      ret =
          inner.stream().filter(q -> q.getTarget() == participant).collect(Collectors.toList());
      if (deleteRetrieved){
        inner.removeAll(ret);
        ret.stream().forEach(q -> {recorder.recordMessage(MessengerRecorder.eAction.GET, q) ;});
      }
    }
    return ret;
  }

}
