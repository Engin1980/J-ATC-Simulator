package eng.jAtcSim.newLib.messaging;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Messenger {

  public static class ListenerInfo {
    public final Object key;
    public final Participant messageTarget;
    public final IList<Message> queue;

    public ListenerInfo(Object key, Participant messageTarget) {
      this.key = key;
      this.messageTarget = messageTarget;
      this.queue = new EList<>();
    }
  }
  public static final Participant SYSTEM = Participant.createSystem();
  private IList<ListenerInfo> listeners = new EList<>();
  private final MessengerLog messengerLog = new MessengerLog("messenger_log.txt");

  public IList<Message> getMessagesByListener(Participant listener, boolean deleteRetrieved) {
    ListenerInfo li;
    IList<Message> ret;

    synchronized (this) {
      try {
        li = listeners.getFirst(q -> q.key.equals(listener));
      } catch (Exception ex) {
        throw new EApplicationException("Listener " + listener.toString() + " has not been registered.");
      }
      ret = new EList(li.queue);
      if (deleteRetrieved) {
        li.queue.clear();
      }
    }

    ret.forEach(q -> messengerLog.recordMessage(MessengerLog.eMessageAction.GET, q));

    return ret;
  }

  public void registerCustomListener(Object key, Participant messageTarget) {
    EAssert.Argument.isNotNull(key, "key");
    EAssert.Argument.isNotNull(messageTarget, "messageTarget");
    if (listeners.isAny(q -> q.key.equals(key)))
      throw new EApplicationException("Listener " + key + " already registered.");

    ListenerInfo li = new ListenerInfo(key, messageTarget);
    this.listeners.add(li);

    messengerLog.recordRegistratin(MessengerLog.eRegistrationAction.REGISTER, key);
  }

  public void registerListener(Participant listener) {
    registerCustomListener(listener, listener);
    messengerLog.recordRegistratin(MessengerLog.eRegistrationAction.REGISTER, listener);
  }

  public void send(Message msg) {
    synchronized (this) {
      listeners
          .where(q -> q.messageTarget.equals(msg.getTarget()))
          .forEach(q -> q.queue.add(msg));
      messengerLog.recordMessage(MessengerLog.eMessageAction.ADD, msg);
    }
  }

  public void unregisterCustomListener(Object key) {
    EAssert.Argument.isNotNull(key, "key");
    ListenerInfo li = listeners.tryGetFirst(q -> q.key.equals(key));
    if (li == null)
      throw new EApplicationException(sf("There is no listener object '%s'.", key));

    listeners.remove(li);
  }

  public void unregisterListener(Participant listener) {
    unregisterCustomListener(listener);
  }
}
