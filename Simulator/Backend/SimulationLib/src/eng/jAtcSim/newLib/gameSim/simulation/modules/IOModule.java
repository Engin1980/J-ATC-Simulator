package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
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
import eng.jAtcSim.newLib.speeches.base.Rejection;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;

public class IOModule extends SimulationModule {

  private final KeyShortcutManager keyShortcutManager;
  private final Messenger messenger;
  private final ParserFormatterStartInfo parseFormatStartInfo;
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

  public IList<Message> getMessagesByKey(Object listener) {
    IList<eng.jAtcSim.newLib.messaging.Message> tmp = this.messenger.getMessagesByListener(listener, true);
    return tmp;
  }

  public ParserFormatterStartInfo getParserFormatterInfo() {
    return this.parseFormatStartInfo;
  }

  public void init() {
    IMessagingAcc messagingContext = new MessagingAcc(messenger);
    ContextManager.setContext(IMessagingAcc.class, messagingContext);
    this.systemMessagesModule.init();
  }

  public void registerMessageListener(Object listener, Messenger.ListenerAim ... aims){
    this.messenger.registerListener(listener, aims);
  }

  public void registerMessagesListener(Participant participant) {
    registerMessageListener(participant, new Messenger.ListenerAim(participant, Messenger.eListenerDirection.receiver));
  }

  public void unregisterMessageListener(Object listener){
    this.messenger.unregisterListener(listener);
  }

  public void sendAtcCommand(AtcId id, IAtcSpeech atcSpech) {
    eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
        Participant.createAtc(this.userAtcId),
        Participant.createAtc(id),
        atcSpech
    );
    this.messenger.send(msg);
  }

  public void sendPlaneCommand(Callsign callsign, SpeechList<IForPlaneSpeech> cmds) {
    eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
        Participant.createAtc(this.userAtcId),
        Participant.createAirplane(callsign),
        cmds
    );
    this.messenger.send(msg);
  }

  public void sendSystemCommand(ISystemSpeech systemSpeech) {
    eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
        Participant.createAtc(userAtcId),
        Participant.createSystem(),
        systemSpeech
    );
    this.messenger.send(msg);
  }

  public void sendTextMessageForUser(IMessageContent content) {
    eng.jAtcSim.newLib.messaging.Message m = new eng.jAtcSim.newLib.messaging.Message(
        Participant.createSystem(),
        Participant.createAtc(this.userAtcId),
        content);
    Context.getMessaging().getMessenger().send(m);
  }

}
