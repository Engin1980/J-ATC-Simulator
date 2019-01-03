/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Airplanes;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marek
 */
public class UserAtc extends Atc {

  public enum eErrorBehavior {

    throwExceptions,
    sendSystemErrors
  }

  private final Parser parser = new ShortBlockParser();
  private eErrorBehavior errorBehavior = eErrorBehavior.sendSystemErrors;

  public UserAtc(AtcTemplate template) {
    super(template);
  }

  public eErrorBehavior getErrorBehavior() {
    return errorBehavior;
  }

  public void setErrorBehavior(eErrorBehavior errorBehavior) {
    this.errorBehavior = errorBehavior;
  }

  public void elapseSecond() {

  }

  public void sendToPlane(String line) {
    String[] tmp = splitToCallsignAndMessages((line));
    String airplaneCallsignOrPart = tmp[0];
    String commands = tmp[1];

    Airplane p = Airplanes.tryGetByCallsingOrNumber(Acc.planes(), airplaneCallsignOrPart);
    if (p == null) {
      raiseError(
          "Cannot identify airplane under callsign (or part) \"" + airplaneCallsignOrPart + "\". None or multiple planes identified.");
      return;
    }

    SpeechList cmdList;
    try {
      cmdList = parser.parseMulti(commands);
    } catch (Exception ex) {
      raiseError(ex.getMessage());
      return;
    }
    sendToPlane(p, cmdList);
  }

  public void sendToPlane(Callsign c, SpeechList speeches) {
    Airplane pln = Airplanes.tryGetByCallsign(Acc.planes(), c);
    if (pln == null) {
      raiseError("No such plane for callsign \"" + c.toString() + "\".");
      return;
    }
    sendToPlane(pln, speeches);
  }

  public void sendToAtc(Atc.eType type, String message) {
    if (message.matches("\\d{4}")) {
      String[] tmp = RegexUtils.extractGroups(message, "^(\\d{4})(.*)$");
      Squawk s = Squawk.tryCreate(tmp[0]);
      if (s == null) {
        raiseError("\"" + tmp[0] + "\" is not valid transponder code.");
        return;
      }
      Airplane plane = Airplanes.tryGetBySqwk(Acc.planes(), s);
      if (plane == null) {
        raiseError("SQWK " + s.toString() + " does not exist.");
        return;
      }
      sendToAtc(type, plane, tmp[1]);
    } else {
      try {
        IAtc2Atc content = parser.parseAtc(message);
        sendToAtc(type, content);
      } catch (Exception ex) {
        raiseError("\"" + message + "\" has invalid syntax as message for ATC");
      }
    }
  }

  public void sendToAtc(Atc.eType type, IAtc2Atc msg) {
    Atc atc = Acc.atc(type);
    Message m = new Message(this, atc, msg);
    super.sendMessage(m);
  }

  public void sendToAtc(Atc.eType type, Airplane plane, String additionalMessage) {
    Atc atc = Acc.atc(type);

    if (getPrm().xisUnderSwitchRequest(plane, null,this)) {
      // je to ... -> APP

      if (getPrm().getResponsibleAtc(plane).getType() != type) {
        // nesedi smer potvrzeni A predava na APP, ale APP potvrzuje na B
        sendError("SQWK " + plane.getSqwk() + " not under your control. You cannot request switch.");
        return;
      } else {
//        // overime routovani
//        String[] tmp = decodeAdditionalRouting(additionalMessage);
//        // poprosime ATC o potvrzeni routovani
//        if (tmp != null && atc.acceptsChangedRouting(plane, tmp[0], tmp[1])){
//          // potvrdime
//          getPrm().confirmSwitch(this, plane);
//        } else {
//          // zamitneme zmenene routovani
//          getPrm().abortSwitch(plane, atc, Acc.atcApp());
//        }
        getPrm().confirmSwitch(this, plane);
      }
    } else {
      if (getPrm().getResponsibleAtc(plane).equals(this))
        // je nova zadost APP -> ???
        getPrm().requestSwitch(this, atc, plane);
      else
        // je to zruseni zadosti o switch
        getPrm().requestSwitch(getPrm().getResponsibleAtc(plane), this, plane);
    }

    PlaneSwitchMessage msg = new PlaneSwitchMessage(plane);
    Message m = new Message(this, atc, msg);
    super.sendMessage(m);
  }

  public void sendError(String message) {
    Message m = new Message(Messenger.SYSTEM, this, new StringMessageContent(message));
    super.sendMessage(m);
  }

  public void sendSystem(String message) {
    if (message.trim().isEmpty()) {
      message = "?";
    }
    Message m = new Message(this, Messenger.SYSTEM, new StringMessageContent(message.trim()));
    super.sendMessage(m);
  }

  public Parser getParser() {
    return parser;
  }

  private String[] decodeAdditionalRouting(String text) {
    Matcher m =
        Pattern.compile("(\\d{1,2}[lrcLRC]?)?(\\/.+)?")
            .matcher(text);
    String[] ret = {m.group(1), m.group(2)};
    return ret;
  }

  private String[] splitToCallsignAndMessages(String msg) {
    String[] ret = new String[2];
    int i = msg.indexOf(" ");
    if (i == msg.length() || i < 0) {
      ret[0] = msg;
      ret[1] = "";
    } else {
      ret[0] = msg.substring(0, i);
      ret[1] = msg.substring(i + 1);
    }
    return ret;
  }

  @Override
  protected void _save(XElement elm) {

  }

  @Override
  protected void _load(XElement elm) {

  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane) {

  }

  @Override
  public void removePlaneDeletedFromGame(Airplane plane) {

  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean finalRegistration) {

  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  private void raiseError(String text) {
    recorder.write(this, "ERR", text);
    switch (this.errorBehavior) {
      case sendSystemErrors:
        sendError(text);
        break;
      case throwExceptions:
        throw new ERuntimeException(text);
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void sendToPlane(Airplane plane, SpeechList speeches) {
    Message m = new Message(this, plane, speeches);
    super.sendMessage(m);
  }
}
