package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.IMessage;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
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

public class IOModule extends SimulationModule {

  private final KeyShortcutManager keyShortcutManager;
  private final Messenger messenger;
  private final ParserFormatterStartInfo parseFormatStartInfo;
  private IMap<Object, IList<IMessage>> storedMessages = new EMap<>();
  private IMap<Object, Participant> storedMessagesReceiverRegistrations = new EMap<>();
  private IMap<Object, Participant> storedMessagesSenderRegistrations = new EMap<>();
  private final SystemMessagesModule systemMessagesModule;
  private final AtcId userAtcId;

  public IOModule(
      Simulation parent,
      AtcId userAtcId,
      KeyShortcutManager keyShortcutManager,
      ParserFormatterStartInfo parserFormatterStartInfo,
      SystemMessagesModule systemMessagesModule) {
    super(parent);
    EAssert.Argument.isNotNull(keyShortcutManager, "keyShortcutManager");
    EAssert.Argument.isNotNull(parserFormatterStartInfo, "parserFormatterStartInfo");
    EAssert.Argument.isNotNull(systemMessagesModule, "systemMessagesModule");

    this.userAtcId = userAtcId;
    this.keyShortcutManager = keyShortcutManager;
    this.parseFormatStartInfo = parserFormatterStartInfo;
    this.systemMessagesModule = systemMessagesModule;
    this.messenger = new Messenger();
  }

  public void elapseSecond() {
    systemMessagesModule.elapseSecond();
  }

  public KeyShortcutManager getKeyShortcutManager() {
    return keyShortcutManager;
  }

  public IList<IMessage> getMessagesByKey(Object key) {
    IList<IMessage> ret = new EList<>(storedMessages.get(key));
    storedMessages.get(key).clear();
    return ret;
  }

  public void init() {
    IMessagingAcc messagingContext = new MessagingAcc(messenger);
    ContextManager.setContext(IMessagingAcc.class, messagingContext);
    this.systemMessagesModule.init();
  }

  public void registerMessagesListenerByReceiver(Object key, Participant messageReceiver) {
    tady se to registruje pres nejakou tridu, ale ta to zrejme vubec netaha pres messenger. WHT?
    this.storedMessages.set(key, new EList<>());
    this.storedMessagesReceiverRegistrations.set(key, messageReceiver);
  }

  public void registerMessagesListenerBySender(Object key, Participant messageSender) {
    this.storedMessages.set(key, new EList<>());
    this.storedMessagesSenderRegistrations.set(key, messageSender);
  }

  public void sendAtcCommand(AtcId id, IAtcSpeech atcSpech) {
    Message msg = new Message(
        Participant.createAtc(this.userAtcId),
        Participant.createAtc(id),
        atcSpech
    );
    this.messenger.send(msg);
  }

  public void sendPlaneCommand(Callsign callsign, SpeechList<IForPlaneSpeech> cmds) {
    Message msg = new Message(
        Participant.createAtc(this.userAtcId),
        Participant.createAirplane(callsign),
        cmds
    );
    this.messenger.send(msg);
  }

  public void sendSystemCommand(ISystemSpeech systemSpeech) {
    Message msg = new Message(
        Participant.createAtc(userAtcId),
        Participant.createSystem(),
        systemSpeech
    );
    this.messenger.send(msg);
  }

  public void sendTextMessageForUser(IMessageContent content) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(this.userAtcId),
        content);
    Context.getMessaging().getMessenger().send(m);
  }

}
