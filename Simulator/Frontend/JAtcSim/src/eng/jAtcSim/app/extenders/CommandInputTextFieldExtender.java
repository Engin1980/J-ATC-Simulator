package eng.jAtcSim.app.extenders;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.events.Event;
import eng.eSystem.exceptions.ERuntimeException;
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
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CommandInputTextFieldExtender {

  public enum SpecialCommandType {
    storeRadarPosition,
    recallRadarPosition,
    toggleSimulatorRun
  }

  public enum ErrorType {
    atcUnableParse,
    systemUnableParse, planeMultipleCallsignMatches, planeNoneCallsignMatch, planeUnableParse, atcUnableDecide
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
    public final IReadOnlyMap<String, Object> arguments;

    public ErrorEventArgs(ErrorType error, IReadOnlyMap<String, Object> arguments) {
      EAssert.Argument.isNotNull(arguments, "arguments");

      this.error = error;
      this.arguments = arguments;
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

  public CommandInputTextFieldExtender(JTextField txt,
                                       ParsersSet parsers,
                                       Producer<IReadOnlyList<AtcId>> atcIdsProducer,
                                       Producer<IReadOnlyList<Callsign>> planeCallsignsProducer) {
    this.txt = txt;
    this.parsers = parsers;
    this.atcIdsProducer = atcIdsProducer;
    this.planeCallsignsProducer = planeCallsignsProducer;
    this.assignListeners();
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

  public void send() {
    this.sendMessageFromText();
  }

  private void raiseSpecialCommand(SpecialCommandType specialCommandType, EMap<String, Object> attributes) {
    SpecialCommandEventArgs e = new SpecialCommandEventArgs(specialCommandType, attributes);
    this.onSpecialCommand.raise(e);
  }

  private void assignListeners() {
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
          case KeyEvent.VK_F1:
            raiseSpecialCommand(SpecialCommandType.toggleSimulatorRun, new EMap<>());
            break;
          case KeyEvent.VK_F2:
            if (isCtr)
              raiseSpecialCommand(SpecialCommandType.storeRadarPosition, EMap.of("bank", 2));
            else
              raiseSpecialCommand(SpecialCommandType.recallRadarPosition, EMap.of("bank", 2));
            break;
          case KeyEvent.VK_F3:
            if (isCtr)
              raiseSpecialCommand(SpecialCommandType.storeRadarPosition, EMap.of("bank", 3));
            else
              raiseSpecialCommand(SpecialCommandType.recallRadarPosition, EMap.of("bank", 3));
            break;
          case KeyEvent.VK_F4:
            if (isCtr)
              raiseSpecialCommand(SpecialCommandType.storeRadarPosition, EMap.of("bank", 4));
            else
              raiseSpecialCommand(SpecialCommandType.recallRadarPosition, EMap.of("bank", 4));
            break;
          case KeyEvent.VK_F5:
            if (isCtr)
              raiseSpecialCommand(SpecialCommandType.storeRadarPosition, EMap.of("bank", 5));
            else
              raiseSpecialCommand(SpecialCommandType.recallRadarPosition, EMap.of("bank", 5));
            break;
          case KeyEvent.VK_F6:
            if (isCtr)
              raiseSpecialCommand(SpecialCommandType.storeRadarPosition, EMap.of("bank", 6));
            else
              raiseSpecialCommand(SpecialCommandType.recallRadarPosition, EMap.of("bank", 6));
            break;
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
    } catch (Throwable t) {
      throw new ERuntimeException("Message invocation failed for item: " + msg, t);
    }
  }

  private void processPlaneMessage(String msg) {
    String[] pts = splitCallsignAndMessage(msg);
    Callsign callsign;
    {
      Tuple<Callsign, ErrorType> t = getCallsignFromString(pts[0]);
      if (t.getB() != null) {
        raiseError(t.getB(), EMap.of("callsign", pts[0]));
        return;
      }
      callsign = t.getA();
    }

    IPlaneParser parser = parsers.planeParser;
    SpeechList<IForPlaneSpeech> cmds;
    try {
      cmds = parser.parse(pts[1]);
    } catch (Exception e) {
      raiseError(ErrorType.planeUnableParse, EMap.of("callsign", pts[0], "command", pts[1]));
      return;
    }

    raisePlaneCommand(callsign, cmds);
    this.clear();
    this.focus();
  }

  private void raisePlaneCommand(Callsign callsign, SpeechList<IForPlaneSpeech> cmds) {
    CommandEventArgs<Callsign, SpeechList<IForPlaneSpeech>> e = new CommandEventArgs<>(callsign, cmds);
    this.onPlaneCommand.raise(e);
  }

  private Tuple<Callsign, ErrorType> getCallsignFromString(String callsignString) {
    IReadOnlyList<Callsign> clsgns = this.planeCallsignsProducer.invoke();
    Callsign ret = clsgns.tryGetFirst(q -> q.toString().equals(callsignString));
    if (ret == null) {
      IList<Callsign> tmp = clsgns.where(q -> q.getNumber().equals(callsignString));
      if (tmp.count() > 1)
        return new Tuple<>(null, ErrorType.planeMultipleCallsignMatches);
      else if (tmp.count() == 0)
        return new Tuple<>(null, ErrorType.planeNoneCallsignMatch);
      else
        ret = tmp.getFirst();
    }
    EAssert.isNotNull(ret);
    return new Tuple<>(ret, null);
  }

  private String[] splitCallsignAndMessage(String msg) {
    int firstSpaceIndex = msg.indexOf(' ');
    String callsignString = msg.substring(0, firstSpaceIndex);
    String messageString = msg.substring(firstSpaceIndex + 1);
    return new String[]{callsignString, messageString};
  }

  private void processSystemMessage(String msg) {
    msg = msg.substring(1);
    ISystemParser parser = parsers.systemParser;
    ISystemSpeech speech = null;
    try {
      speech = parser.parse(msg);
    } catch (Exception e) {
      raiseError(ErrorType.systemUnableParse, EMap.of("command", msg));
      return;
    }
    raiseSystemCommand(speech);
    this.clear();
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
    IAtcParser parser = this.parsers.atcParser;
    IAtcSpeech speech;
    try {
      speech = parser.parse(msg);
    } catch (Exception e) {
      this.raiseError(ErrorType.atcUnableParse, EMap.of("command", msg, "cause", e));
      return;
    }
    AtcId id;
    try {
      id = this.atcIdsProducer.invoke().getFirst(q -> q.getType() == atcType);
    } catch (Exception e) {
      this.raiseError(ErrorType.atcUnableDecide, EMap.of("command", msg, "cause", e));
      return;
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

  private void raiseError(ErrorType type, EMap<String, Object> arguments) {
    ErrorEventArgs e = new ErrorEventArgs(type, arguments);
    this.onError.raise(e);
  }
}
