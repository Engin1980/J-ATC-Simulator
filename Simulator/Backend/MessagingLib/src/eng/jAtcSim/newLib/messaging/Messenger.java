package eng.jAtcSim.newLib.messaging;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Messenger {

  public static class ListenerAim {
    private final eListenerDirection direction;
    private final Participant participant;

    public ListenerAim(Participant participant, eListenerDirection direction) {
      this.participant = participant;
      this.direction = direction;
    }

    public eListenerDirection getDirection() {
      return direction;
    }

    public Participant getParticipant() {
      return participant;
    }
  }

  public static class ListenerInfo {
    private final IList<ListenerAim> aims = new EList<>();
    private final IList<Message> queue = new EList<>();

    public ListenerInfo(ListenerAim... aims) {
      this.aims.addMany(aims);
    }

    public IReadOnlyList<ListenerAim> getAims() {
      return aims;
    }
  }

  public enum eListenerDirection {
    sender,
    receiver
  }

  public static final Participant SYSTEM = Participant.createSystem();
  private final IMap<Object, ListenerInfo> listeners = new EMap<>();
  private final MessengerLog messengerLog = new MessengerLog("messenger_log.txt");

  public IList<Message> getMessagesByListener(Object listener, boolean deleteRetrieved) {
    EAssert.Argument.isNotNull(listener, "listener");

    ListenerInfo lis;
    IList<Message> ret;

    synchronized (this) {
      try {
        lis = listeners.get(listener);
      } catch (Exception ex) {
        throw new EApplicationException("Listener " + listener.toString() + " has not been registered.");
      }
      ret = new EList<>(lis.queue);
      if (deleteRetrieved) {
        lis.queue.clear();
      }
    }

    ret.forEach(q -> messengerLog.recordMessage(MessengerLog.eMessageAction.GET, q));

    return ret;
  }

  public void registerListener(Participant listener) {
    ListenerAim listenerAim = new ListenerAim(listener, eListenerDirection.receiver);
    registerListener(listener, listenerAim);
  }

  public void registerListener(Object listener, ListenerAim... aims) {
    EAssert.Argument.isNotNull(listener, "listener");
    EAssert.Argument.isNotNull(aims, "aims");
    EAssert.Argument.isTrue(aims.length > 0, "Aims must be non empty");
    EAssert.Argument.isFalse(this.listeners.containsKey(listener), sf("Listener '%s' is already registered.", listener));

    ListenerInfo listenerInfo = new ListenerInfo(aims);
    listeners.set(listener, listenerInfo);

    messengerLog.recordRegistratin(MessengerLog.eRegistrationAction.REGISTER, listener);
  }

  public void send(Message msg) {
    synchronized (this) {
      for (ListenerInfo listenerInfo : listeners.getValues()) {
        if (listenerInfo.getAims().isAny(q ->
            (q.direction == eListenerDirection.sender && q.participant.equals(msg.getSource()))
            ||
            (q.direction == eListenerDirection.receiver && q.participant.equals(msg.getTarget())))) {
          listenerInfo.queue.add(msg);
        }
      }
      messengerLog.recordMessage(MessengerLog.eMessageAction.ADD, msg);
    }
  }

  public void unregisterListener(Object listener) {
    EAssert.Argument.isNotNull(listener, "listener");
    EAssert.Argument.isTrue(listeners.containsKey(listener), "Listener is not registered.");

    this.listeners.remove(listener);
  }
}
