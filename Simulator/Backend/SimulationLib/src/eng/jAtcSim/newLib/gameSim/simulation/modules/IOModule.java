package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.IUserAtcInterface;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.KeyShortcutManager;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.context.IMessagingAcc;
import eng.jAtcSim.newLib.messaging.context.MessagingAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import exml.IXPersistable;

public class IOModule extends SimulationModule {

  private static class OnlyOnePlaneMessageAtOnceProxy implements IXPersistable {
    private static final int DELAY_BETWEEN_PLANE_MESSAGES_IN_SECONDS = 6;
    private int counter = 0;
    private final IList<Message> inner = new EList<>();

    public void processMessages(IList<Message> lst) {
      filterOutPlaneMessages(lst);
      appendPlaneMessageIfReady(lst);
    }

    private void appendPlaneMessageIfReady(IList<Message> lst) {
      if (inner.size() > 0) {
        if (counter <= 0) {
          Message msg = inner.get(0);
          inner.removeAt(0);
          lst.add(msg);
          counter = DELAY_BETWEEN_PLANE_MESSAGES_IN_SECONDS;
        }
      }
      counter--;
    }

    private void filterOutPlaneMessages(IList<Message> lst) {
      IList<Message> tmp = lst.where(q -> q.getSource().getType() == Participant.eType.airplane);
      lst.removeMany(tmp);
      inner.addMany(tmp);
    }
  }

  private final KeyShortcutManager keyShortcutManager;
  private final Messenger messenger;
  private final SystemMessagesModule systemMessagesModule;
  private final OnlyOnePlaneMessageAtOnceProxy onlyOnePlaneMessageAtOnceProxy = new OnlyOnePlaneMessageAtOnceProxy();

  public IOModule(
          Simulation parent,
          KeyShortcutManager keyShortcutManager,
          SystemMessagesModule systemMessagesModule) {
    super(parent);
    EAssert.Argument.isNotNull(keyShortcutManager, "keyShortcutManager");
    EAssert.Argument.isNotNull(systemMessagesModule, "systemMessagesModule");

    this.keyShortcutManager = keyShortcutManager;
    this.systemMessagesModule = systemMessagesModule;
    this.messenger = new Messenger();
  }

  public void elapseSecond() {
    systemMessagesModule.elapseSecond();
  }

  public KeyShortcutManager getKeyShortcutManager() {
    return keyShortcutManager;
  }

  public IList<Message> getMessagesByKey(Object listener) {
    IList<eng.jAtcSim.newLib.messaging.Message> tmp = this.messenger.getMessagesByListener(listener, true);
    this.onlyOnePlaneMessageAtOnceProxy.processMessages(tmp);
    return tmp;
  }

  public void init() {
    IMessagingAcc messagingContext = new MessagingAcc(messenger);
    ContextManager.setContext(IMessagingAcc.class, messagingContext);
    this.systemMessagesModule.init();
  }

  public void registerMessageListener(Object listener, Messenger.ListenerAim... aims) {
    this.messenger.registerListener(listener, aims);
  }

  public void registerMessagesListener(Participant participant) {
    registerMessageListener(participant, new Messenger.ListenerAim(participant, Messenger.eListenerDirection.receiver));
  }

  public void sendAtcCommand(AtcId fromAtcId, AtcId toAtcId, IAtcSpeech atcSpeech) {
    IUserAtcInterface humanAtc = getUserAtcInterface(fromAtcId);
    humanAtc.sendAtcCommand(toAtcId, atcSpeech);
  }

  public void sendPlaneCommand(AtcId fromAtcId, Callsign toCallsign, SpeechList<IForPlaneSpeech> cmds) {
    IUserAtcInterface humanAtc = getUserAtcInterface(fromAtcId);
    humanAtc.sendPlaneCommand(toCallsign, cmds);
  }

  public void sendSystemCommand(AtcId fromAtcId, ISystemSpeech systemSpeech) {
    IUserAtcInterface humanAtc = getUserAtcInterface(fromAtcId);
    humanAtc.sendSystemCommand(systemSpeech);
  }

  public void sendSystemCommandByGame(AtcId targetAtcId, ISystemSpeech systemSpeech) {
    messenger.send(
            new Message(
                    Participant.createSystem(),
                    Participant.createAtc(targetAtcId),
                    systemSpeech));
  }

  public void sendTextMessageForUser(AtcId targetAtcId, IMessageContent content) {
    eng.jAtcSim.newLib.messaging.Message m = new eng.jAtcSim.newLib.messaging.Message(
            Participant.createSystem(),
            Participant.createAtc(targetAtcId),
            content);
    Context.getMessaging().getMessenger().send(m);
  }

  public void unregisterMessageListener(Object listener) {
    this.messenger.unregisterListener(listener);
  }

  private IUserAtcInterface getUserAtcInterface(AtcId fromAtcId) {
    IUserAtcInterface ret = parent.getAtcModule().getUserAtcInterface(fromAtcId);
    return ret;
  }

}
