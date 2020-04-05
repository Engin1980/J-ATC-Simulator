package eng.jAtcSim.newLib.messaging;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;

public class Messenger {

  public static class ListenerInfo {
    public final Object listener;
    public final Participant messageTarget;
    public final IList<Message> queue;

    public ListenerInfo(Object listener, Participant messageTarget) {
      this.listener = listener;
      this.messageTarget = messageTarget;
      this.queue = new EList<>();
    }
  }
  //TODO recorder reimplementation
//  @XmlIgnore
//  private MessengerRecorder recorder = new MessengerRecorder("Messenger log", "messenger.log");
  public static final Participant SYSTEM = Participant.createSystem();
  private IList<ListenerInfo> listeners = new EList<>();

  public void registerListener(Object listener, Participant messageTarget) {
    EAssert.isNotNull(listener);
    EAssert.isNotNull(messageTarget);
    if (listeners.isAny(q -> q.listener == listener))
      throw new EApplicationException("Listener " + listener.toString() + " already registered.");

    ListenerInfo li = new ListenerInfo(listener, messageTarget);
    this.listeners.add(li);
  }

  public void unregisterListener(Object listener) {
    EAssert.isNotNull(listener);
    ListenerInfo li = listeners.getFirst(q -> q.listener == listener);
    listeners.remove(li);
  }

  public void send(Message msg) {
    synchronized (this) {
      listeners
          .where(q -> q.messageTarget == msg.getTarget())
          .forEach(q -> q.queue.add(msg));
//      recorder.recordMessage(MessengerRecorder.eAction.ADD, msg);
    }
  }

  public IList<Message> getMessagesByListener(Object listener, boolean deleteRetrieved) {
    ListenerInfo li;
    IList<Message> ret;

    synchronized (this) {
      try {
        li = listeners.getFirst(q -> q.listener == listener);
      } catch (Exception ex){
        throw new EApplicationException("Listener " + listener.toString() + " has not been registered.");
      }
      ret = new EList(li.queue);
      if (deleteRetrieved) {
        li.queue.clear();
//        ret.forEach(q->recorder.recordMessage(MessengerRecorder.eAction.GET,q ));
      }
    }

    return ret;

  }
}
