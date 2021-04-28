package eng.jAtcSim.app.extenders;

import eng.eSystem.collections.*;
import eng.eSystem.events.Event;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.functionalInterfaces.Action;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParsersSet;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParsingProvider;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class CommandInputTextFieldExtender {

  public enum SpecialCommandType {
    storeRadarPosition,
    recallRadarPosition,
    toggleSimulatorRun
  }

  public enum ErrorType {
    atcUnableParse,
    systemUnableParse, planeMultipleCallsignMatches, planeNoneCallsignMatch, planeUnableParse, planeUnableFindCallsign, atcUnableDecide
  }

  private static class InputFormatException extends RuntimeException {
    private final String command;
    private final ErrorType type;

    public InputFormatException(ErrorType type, String command, String message, Throwable cause) {
      super(message, cause);
      this.command = command;
      this.type = type;
    }

    public String getCommand() {
      return command;
    }

    public ErrorType getType() {
      return type;
    }
  }

  public static class SpecialCommandEventArgs {
    public final SpecialCommandType specialCommand;
    public final IReadOnlyMap<String, Object> attributes;

    public SpecialCommandEventArgs(SpecialCommandType specialCommand, IReadOnlyMap<String, Object> attributes) {
      this.specialCommand = specialCommand;
      this.attributes = attributes;
    }
  }

  public static class CommandEventArgs<TTarget, TCommand> {
    public final TTarget target;
    public final TCommand command;

    public CommandEventArgs(TTarget target, TCommand command) {
      this.target = target;
      this.command = command;
    }
  }

  public static class ErrorEventArgs {
    public final ErrorType error;
    public final String message;
    public final String command;
    public final String cause;

    public ErrorEventArgs(ErrorType error, String message, String command, String cause) {
      this.error = error;
      this.message = message;
      this.command = command;
      this.cause = cause;
    }
  }

  public final Event<CommandInputTextFieldExtender, SpecialCommandEventArgs> onSpecialCommand = new Event<>(this);
  public final Event<CommandInputTextFieldExtender, CommandEventArgs<AtcId, IAtcSpeech>> onAtcCommand = new Event<>(this);
  public final Event<CommandInputTextFieldExtender, CommandEventArgs<Callsign, SpeechList<IForPlaneSpeech>>> onPlaneCommand = new Event<>(this);
  public final Event<CommandInputTextFieldExtender, CommandEventArgs<Object, ISystemSpeech>> onSystemCommand = new Event<>(this);
  public final Event<CommandInputTextFieldExtender, ErrorEventArgs> onError = new Event<>(this);
  private final Producer<IReadOnlyList<AtcId>> atcIdsProducer;
  private final Producer<IReadOnlyList<Callsign>> planeCallsignsProducer;
  private final JTextField txt;
  private final ParsersSet parsers;
  private boolean isCtr = false;
  private final IMap<eng.jAtcSim.newPacks.utils.KeyStroke, Action> keyStrokes = new EMap<>();

  public CommandInputTextFieldExtender(JTextField txt,
                                       ParsersSet parsers,
                                       Producer<IReadOnlyList<AtcId>> atcIdsProducer,
                                       Producer<IReadOnlyList<Callsign>> planeCallsignsProducer) {
    this.txt = txt;
    this.parsers = parsers;
    this.atcIdsProducer = atcIdsProducer;
    this.planeCallsignsProducer = planeCallsignsProducer;
    this.assignKeyListeners();
  }

  public CommandInputTextFieldExtender(ParsersSet parsers,
                                       Producer<IReadOnlyList<AtcId>> atcIdsProducer,
                                       Producer<IReadOnlyList<Callsign>> planeCallsignsProducer) {
    this(new JFormattedTextField(), parsers, atcIdsProducer, planeCallsignsProducer);
  }

  public void appendText(String text, boolean separate) {
    String tmp;
    if (separate)
      tmp = this.txt.getText() + " " + text + " ";
    else
      tmp = this.txt.getText() + text;
    this.txt.setText(tmp);
  }

  public void erase() {
    this.txt.setText("");
  }

  public void focus() {
    this.txt.requestFocus();
  }

  public JTextField getControl() {
    return this.txt;
  }

  public void registerKeyStroke(eng.jAtcSim.newPacks.utils.KeyStroke keyStroke, Action a) {
    keyStrokes.set(keyStroke, a);
  }

  public void send() {
    this.sendMessageFromText();
  }

  private void raiseSpecialCommand(SpecialCommandType specialCommandType, EMap<String, Object> attributes) {
    SpecialCommandEventArgs e = new SpecialCommandEventArgs(specialCommandType, attributes);
    this.onSpecialCommand.raise(e);
  }

  private void assignKeyListeners() {
    this.txt.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_CONTROL:
            isCtr = true;
            break;
          case java.awt.event.KeyEvent.VK_ESCAPE:
            erase();
            break;
          case java.awt.event.KeyEvent.VK_LEFT:
            if (isCtr) appendText("TL", true);
            break;
          case java.awt.event.KeyEvent.VK_RIGHT:
            if (isCtr) appendText("TR", true);
            break;
          case java.awt.event.KeyEvent.VK_UP:
            if (isCtr) appendText("CM", true);
            break;
          case java.awt.event.KeyEvent.VK_DOWN:
            if (isCtr) appendText("DM", true);
            break;
          case java.awt.event.KeyEvent.VK_ENTER:
            sendMessageFromText();
            break;
          default:
            keyStrokes.getKeys().tryGetFirst(q ->
                    q.isCtr == isCtr && q.keyCode == e.getKeyCode())
                    .ifPresent(q -> keyStrokes.get(q).invoke());
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_CONTROL:
            isCtr = false;
            break;
        }
      }

      @Override
      public void keyTyped(KeyEvent e) {

      }
    });
  }

  private String normalizeMsg(String txt) {
    txt = txt.trim();
    StringBuilder sb = new StringBuilder(txt);
    int doubleSpaceIndex = sb.toString().indexOf("  ");
    while (doubleSpaceIndex >= 0) {
      sb.replace(doubleSpaceIndex, doubleSpaceIndex + 2, " ");
      doubleSpaceIndex = sb.toString().indexOf("  ");
    }
    return sb.toString();
  }

  private void sendMessageFromText() {
    String msg = this.txt.getText();
    msg = this.normalizeMsg(msg);
    try {
      if (this.isAtcMessage(msg)) {
        processAtcMessage(msg);
      } else if (isSystemMessage(msg)) {
        processSystemMessage(msg);
//      } else if (msg.startsWith("!")) {
//        // application
//        processApplicationMessage(msg);
//        ret = true;
      } else {
        processPlaneMessage(msg);
      }
    } catch (InputFormatException e) {
      raiseError(e);
    } catch (Throwable t) {
      throw new ApplicationException(sf("Uncaptured error occured in '%s': %s", this.getClass().getName(), msg), t);
    }
  }

  private void processPlaneMessage(String msg) {
    String[] pts;
    try {
      pts = splitCallsignAndMessage(msg);
    } catch (Exception e) {
      throw new InputFormatException(ErrorType.planeUnableFindCallsign, msg, "Unable to find callsign at the beginning of the command.", e);
    }

    Callsign callsign;
    try {
      callsign = getCallsignFromStringOrThrowError(pts[0].toUpperCase());
    } catch (InputFormatException e) {
      throw new InputFormatException(e.type, msg, e.getMessage(), e.getCause());
    }

    IPlaneParsingProvider parser = parsers.planeParser;
    SpeechList<IForPlaneSpeech> cmds;
    try {
      cmds = parser.parse(pts[1]);
    } catch (Exception e) {
      throw new InputFormatException(ErrorType.planeUnableParse, pts[1], "Unable to understand message for '" + callsign.toString() + "'.", e);
    }

    raisePlaneCommand(callsign, cmds);
    this.clear();
    this.focus();
  }

  private void raisePlaneCommand(Callsign callsign, SpeechList<IForPlaneSpeech> cmds) {
    CommandEventArgs<Callsign, SpeechList<IForPlaneSpeech>> e = new CommandEventArgs<>(callsign, cmds);
    this.onPlaneCommand.raise(e);
  }

  private Callsign getCallsignFromStringOrThrowError(String callsignString) {
    IReadOnlyList<Callsign> clsgns = this.planeCallsignsProducer.invoke();
    Callsign ret = clsgns.tryGetFirst(q -> q.toString(false).equals(callsignString)).orElse(null);
    if (ret == null) {
      IList<Callsign> tmp = clsgns.where(q -> q.getNumber().equals(callsignString));
      if (tmp.count() > 1)
        throw new InputFormatException(ErrorType.planeMultipleCallsignMatches, callsignString,
                sf("Multiple plane matches callsign string '%s'.", callsignString), null);
      else if (tmp.count() == 0)
        throw new InputFormatException(ErrorType.planeNoneCallsignMatch, callsignString,
                sf("No plane matches callsign string '%s'.", callsignString), null);
      else
        ret = tmp.getFirst();
    }
    EAssert.isNotNull(ret);
    return ret;
  }

  private String[] splitCallsignAndMessage(String msg) {
    int firstSpaceIndex = msg.indexOf(' ');
    String callsignString = msg.substring(0, firstSpaceIndex);
    String messageString = msg.substring(firstSpaceIndex + 1);
    return new String[]{callsignString, messageString};
  }

  private void processSystemMessage(String msg) {
    msg = msg.substring(1);
    ISystemParsingProvider parser = parsers.systemParser;
    ISystemSpeech speech;
    try {
      speech = parser.parse(msg);
    } catch (Exception e) {
      throw new InputFormatException(ErrorType.systemUnableParse, msg, "Unable to understand system message.", e);
    }
    raiseSystemCommand(speech);
    this.clear();348134
    this.focus();
  }

  private void raiseSystemCommand(ISystemSpeech speech) {
    this.onSystemCommand.raise(new CommandEventArgs<>(null, speech));
  }

  private boolean isSystemMessage(String msg) {
    return msg.startsWith("?");
  }

  private boolean isAtcMessage(String msg) {
    return msg.startsWith("+") || msg.startsWith("-");
  }

  private void processAtcMessage(String msg) {
    AtcType atcType = msg.startsWith("+") ? AtcType.ctr : AtcType.twr;
    msg = msg.substring(1);
    IAtcParsingProvider parser = this.parsers.atcParser;
    IAtcSpeech speech;
    try {
      speech = parser.parse(msg);
    } catch (Exception e) {
      throw new InputFormatException(ErrorType.atcUnableParse, msg, "Unable to understand the message for ATC.", e);
    }
    AtcId id;
    try {
      id = this.atcIdsProducer.invoke().getFirst(q -> q.getType() == atcType);
    } catch (Exception e) {
      throw new InputFormatException(ErrorType.atcUnableDecide, msg, "Unable to decide among available ATCs.", e);
    }

    this.raiseAtcCommand(id, speech);
    this.clear();
    this.focus();
  }

  private void raiseAtcCommand(AtcId targetAtcId, IAtcSpeech speech) {
    CommandEventArgs<AtcId, IAtcSpeech> e = new CommandEventArgs<>(targetAtcId, speech);
    this.onAtcCommand.raise(e);
  }

  private void clear() {
    this.txt.setText("");
  }

  private void raiseError(InputFormatException e) {
    ErrorEventArgs eea = new ErrorEventArgs(e.getType(), e.getMessage(), e.command,
            e.getCause() == null ? null : e.getCause().getMessage());
    this.onError.raise(eea);
  }
}
