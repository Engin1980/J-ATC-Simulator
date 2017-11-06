package jatcsimlib.messaging;

import java.nio.file.Paths;
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
  private MessageRecorder recorder = new MessageRecorder("messenger.txt", false, true);

  public void send(Message msg) {
    synchronized (inner) {
      inner.add(msg);
      recorder.recordMessage(MessageRecorder.eAction.ADD, msg);
    }
  }

  public List<Message> getByTarget(IMessageParticipant participant, boolean deleteRetrieved) {
    List<Message> ret;
    synchronized (inner) {
      ret =
          inner.stream().filter(q -> q.getTarget() == participant).collect(Collectors.toList());
      if (deleteRetrieved){
        inner.removeAll(ret);
        ret.stream().forEach(q -> {recorder.recordMessage(MessageRecorder.eAction.GET, q) ;});
      }
    }
    return ret;
  }

}
