package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.KeyShortcutManager;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;

public class IOModule {
  private final KeyShortcutManager keyShortcutManager;
  private final ParserFormatterStartInfo parseFormatStartInfo;
  private final SystemMessagesModule systemMessagesModule;

  public IOModule(KeyShortcutManager keyShortcutManager, ParserFormatterStartInfo parserFormatterStartInfo, SystemMessagesModule systemMessagesModule) {
    EAssert.Argument.isNotNull(keyShortcutManager, "keyShortcutManager");
    EAssert.Argument.isNotNull(parserFormatterStartInfo, "parserFormatterStartInfo");
    EAssert.Argument.isNotNull(systemMessagesModule, "systemMessagesModule");

    this.keyShortcutManager = keyShortcutManager;
    this.parseFormatStartInfo = parserFormatterStartInfo;
    this.systemMessagesModule = systemMessagesModule;
  }

  public void elapseSecond() {
    systemMessagesModule.elapseSecond();
  }

  public KeyShortcutManager getKeyShortcutManager() {
    return keyShortcutManager;
  }

  public void init() {
    //TODO Implement this: Implement this
    throw new ToDoException("Implement this");
//    IMessagingContext messagingContext = new MessagingContext( this.messenger);
//    ContextManager.setContext(IMessagingContext.class, messagingContext);
  }

  public void sendTextMessageForUser(MetarNotification metarNotification) {
    //TODO Implement this: Implement this
    throw new ToDoException("Implement this");
  }
}
