package eng.jAtcSim.newPacks.views;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.app.extenders.CommandInputTextFieldExtender;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParsersSet;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.speeches.system.StringMessage;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.AtcParser;
import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.PlaneParser;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.SystemParser;
import eng.jAtcSim.newPacks.IView;
import eng.jAtcSim.newPacks.context.ViewGlobalEventContext;
import eng.jAtcSim.newPacks.utils.GlobalKeyStrokes;
import eng.jAtcSim.newPacks.utils.ViewGameInfo;

import javax.swing.*;
import java.awt.*;

public class TextInputView implements IView {

  public class CommandInputWrapper {
    public void addCommandTextToLine(char keyChar) {
      TextInputView.this.commandInputTextFieldExtender.appendText(Character.toString(keyChar), false);
    }

    public void addCommandTextToLine(String text) {
      TextInputView.this.commandInputTextFieldExtender.appendText(text, true);
    }

    public void eraseCommand() {
      TextInputView.this.commandInputTextFieldExtender.erase();
    }

    public void sendCommand() {
      TextInputView.this.commandInputTextFieldExtender.send();
    }

    public void setFocus() {
      TextInputView.this.commandInputTextFieldExtender.focus();
    }
  }

  private JPanel parent;
  private CommandInputTextFieldExtender commandInputTextFieldExtender;
  private JTextField txt;
  private ISimulation sim;
  private AtcId userAtcId;
  private ViewGlobalEventContext viewGlobalEventContext;

  @Override
  public void init(JPanel panel, ViewGameInfo initInfo, IReadOnlyMap<String, String> options,
                   ViewGlobalEventContext context) {
    this.parent = panel;
    this.sim = initInfo.getSimulation();
    this.userAtcId = initInfo.getUserAtcId();
    this.viewGlobalEventContext = context;

    this.buildTextField();
    this.parent.setLayout(new BorderLayout());
    this.parent.add(txt, BorderLayout.CENTER);

    registerKeyStrokes();
    context.onUnhandledKeyPress.add(q -> {
      commandInputTextFieldExtender.appendText(Character.toString(q.keyCode), false);
      commandInputTextFieldExtender.focus();
    });
  }

  private void registerKeyStrokes() {

    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.STORE_BANK_2,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.store,
                    2)));
    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.RESTORE_BANK_2,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.restore,
                    2)));

    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.STORE_BANK_3,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.store,
                    3)));
    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.RESTORE_BANK_3,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.restore,
                    3)));

    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.STORE_BANK_4,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.store,
                    4)));
    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.RESTORE_BANK_4,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.restore,
                    4)));

    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.STORE_BANK_5,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.store,
                    5)));
    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.RESTORE_BANK_5,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.restore,
                    5)));

    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.STORE_BANK_6,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.store,
                    6)));
    commandInputTextFieldExtender.registerKeyStroke(
            GlobalKeyStrokes.RESTORE_BANK_6,
            () -> viewGlobalEventContext.onRadarPositionStoreRestore.raise(new ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs(
                    this,
                    ViewGlobalEventContext.RadarPositionStoreRestoreEventArgs.EventAction.restore,
                    6)));
  }

  private void buildTextField() {
    this.txt = new JTextField();
    Font font = new Font("Courier New", Font.BOLD, txt.getFont().getSize() + 8); //todo magic number hack, implement differently
    txt.setFont(font);

    this.commandInputTextFieldExtender = new CommandInputTextFieldExtender(txt,
            this.buildParsers(),
            () -> this.sim.getAtcs(),
            () -> this.sim.getPlanesToDisplay().select(q -> q.callsign()));
    this.commandInputTextFieldExtender.onAtcCommand.add(this::sendAtcCommand);
    this.commandInputTextFieldExtender.onSystemCommand.add(this::sendSystemCommand);
    this.commandInputTextFieldExtender.onPlaneCommand.add(this::sendPlaneCommand);
    this.commandInputTextFieldExtender.onSpecialCommand.add(this::processSpecialCommand);
    this.commandInputTextFieldExtender.onError.add(this::processError);
  }

  private void processError(CommandInputTextFieldExtender commandInputTextFieldExtender,
                            CommandInputTextFieldExtender.ErrorEventArgs eea) {
    String s = convertErrorToString(eea);
    StringMessage smn = new StringMessage(s);
    this.sim.sendSystemCommandAnonymous(this.userAtcId, smn);
  }

  private String convertErrorToString(CommandInputTextFieldExtender.ErrorEventArgs eea) {
    EStringBuilder sb = new EStringBuilder();

    sb.append(eea.message).append(" (Command: ").append(eea.command).append(")");


    //TODEL if not used
//    switch (eea.error) {
//      case atcUnableDecide:
//        sb.appendFormat("Unable to decide target ATC from '%s'.", arguments.get("command"));
//        break;
//      case atcUnableParse:
//        sb.appendFormat("Unable to parse ATC command from '%s'.", arguments.get("command"));
//        break;
//      case planeMultipleCallsignMatches:
//        sb.appendFormat("Multiple planes matches callsign shortcut from '%s'.", arguments.get("callsign"));
//        break;
//      case planeNoneCallsignMatch:
//        sb.appendFormat("No plane matches callsign/shortcut '%s'.", arguments.get("callsign"));
//        break;
//      case planeUnableParse:
//        sb.appendFormat("Unable to parse plane command for plane '%s' from '%s'.", arguments.get("callsign"), arguments.get("command"));
//        break;
//      case systemUnableParse:
//        sb.appendFormat("Unable to parse system command from '%s.", arguments.get("command"));
//        break;
//      default:
//        throw new EEnumValueUnsupportedException(errorType);
//    }

    return sb.toString();
  }

  private ParsersSet buildParsers() {
    return new ParsersSet(
            new PlaneParser(),
            new AtcParser(),
            new SystemParser());
  }

  private void sendPlaneCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.CommandEventArgs<Callsign, SpeechList<IForPlaneSpeech>> e) {
    this.sim.sendPlaneCommands(this.userAtcId, e.target, e.command);
  }

  private void processSpecialCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.SpecialCommandEventArgs e) {
    switch (e.specialCommand) {
      case recallRadarPosition:
        throw new ToDoException();
        //FIXME
//        this.recallRadarPosition((int) e.attributes.get("bank"));
//        break;
      case storeRadarPosition:
        throw new ToDoException();
        //FIXME
//        this.storeRadarPosition((int) e.attributes.get("bank"));
//        break;
      case toggleSimulatorRun:
        this.sim.pauseUnpauseSim();
        break;
      default:
        throw new EEnumValueUnsupportedException(e.specialCommand);
    }
  }

  private void sendSystemCommand(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.CommandEventArgs<Object, ISystemSpeech> e) {
    this.sim.sendSystemCommand(this.userAtcId, e.command);
  }

  private void sendAtcCommand(
          CommandInputTextFieldExtender commandInputTextFieldExtender,
          CommandInputTextFieldExtender.CommandEventArgs<AtcId, IAtcSpeech> e) {
    this.sim.sendAtcCommand(this.userAtcId, e.target, e.command);
  }

}
