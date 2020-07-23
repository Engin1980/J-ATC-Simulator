package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.IOController;

public class IOModule {
  private final IOController ioController;
  private final ParserFormatterStartInfo parseFormatStartInfo;
  private final SystemMessagesModule systemMessagesModule;

  public IOModule(IOController ioController, ParserFormatterStartInfo parserFormatterStartInfo, SystemMessagesModule systemMessagesModule) {
    EAssert.Argument.isNotNull(ioController, "ioController");
    EAssert.Argument.isNotNull(parserFormatterStartInfo, "parserFormatterStartInfo");
    EAssert.Argument.isNotNull(systemMessagesModule, "systemMessagesModule");

    this.ioController = ioController;
    this.parseFormatStartInfo = parserFormatterStartInfo;
    this.systemMessagesModule = systemMessagesModule;
  }
}
