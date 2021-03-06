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
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.AtcParser;
import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.PlaneParser;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.SystemParser;
import eng.jAtcSim.newPacks.IView;

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
  private ISimulation sim;
  private AtcId userAtcId;

  @Override
  public void init(JPanel panel, ViewInitInfo initInfo) {
    this.parent = panel;
    this.sim = initInfo.getSimulation();
    this.userAtcId = initInfo.getUserAtcId();

    JTextField txt = this.buildTextField();
    this.parent.setLayout(new BorderLayout());
    this.parent.add(txt, BorderLayout.CENTER);
  }

  private JTextField buildTextField() {
    JTextField txtInput = new JTextField();
    Font font = new Font("Courier New", Font.PLAIN, txtInput.getFont().getSize());
    txtInput.setFont(font);

    this.commandInputTextFieldExtender = new CommandInputTextFieldExtender(txtInput,
            this.buildParsers(),
            () -> this.sim.getAtcs(),
            () -> this.sim.getPlanesToDisplay().select(q -> q.callsign()));
    this.commandInputTextFieldExtender.onAtcCommand.add(this::sendAtcCommand);
    this.commandInputTextFieldExtender.onSystemCommand.add(this::sendSystemCommand);
    this.commandInputTextFieldExtender.onPlaneCommand.add(this::sendPlaneCommand);
    this.commandInputTextFieldExtender.onSpecialCommand.add(this::processSpecialCommand);
    this.commandInputTextFieldExtender.onError.add(this::processError);

    return txtInput;
  }


  private void processError(CommandInputTextFieldExtender commandInputTextFieldExtender, CommandInputTextFieldExtender.ErrorEventArgs e) {
    String s = convertErrorToString(e.error, e.arguments);
    throw new ToDoException("Somehow let error in this is propagated let radar can show it.");
//    this.radar.showMessageOnScreen(s);
  }


  private String convertErrorToString(CommandInputTextFieldExtender.ErrorType errorType, IReadOnlyMap<String, Object> arguments) {
    EStringBuilder sb = new EStringBuilder();

    switch (errorType) {
      case atcUnableDecide:
        sb.appendFormat("Unable to decide target ATC from '%s'.", arguments.get("command"));
        break;
      case atcUnableParse:
        sb.appendFormat("Unable to parse ATC command from '%s'.", arguments.get("command"));
        break;
      case planeMultipleCallsignMatches:
        sb.appendFormat("Multiple planes matches callsign shortcut from '%s'.", arguments.get("callsign"));
        break;
      case planeNoneCallsignMatch:
        sb.appendFormat("No plane matches callsign/shortcut '%s'.", arguments.get("callsign"));
        break;
      case planeUnableParse:
        sb.appendFormat("Unable to parse plane command for plane '%s' from '%s'.", arguments.get("callsign"), arguments.get("command"));
        break;
      case systemUnableParse:
        sb.appendFormat("Unable to parse system command from '%s.", arguments.get("command"));
        break;
      default:
        throw new EEnumValueUnsupportedException(errorType);
    }

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
