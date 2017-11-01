package jatcsimlib.newMessaging;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Messenger {

  private List<Message> inner = new LinkedList<>();

  public void add(Message msg) {
    synchronized (inner) {
      inner.add(msg);
    }
  }

  public List<Message> getByTarget(IMessageParticipant participant, boolean deleteRetrieved) {
    List<Message> ret;
    synchronized (inner) {
      ret =
          inner.stream().filter(q -> q.getTarget() == participant).collect(Collectors.toList());
      if (deleteRetrieved){
        inner.removeAll(ret);
      }
    }
    return ret;
  }

}
