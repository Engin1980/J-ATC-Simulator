/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Airplanes;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Airplanes;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {

  private final Parser parser = new ShortParser();

  private void raiseError(String text) {
    recorder.log(this, "ERR", text);
    switch (this.errorBehavior) {
      case sendSystemErrors:
        sendError(text);
        break;
      case throwExceptions:
        throw new ERuntimeException(text);
      default:
        throw new ENotSupportedException();
    }
  }

  public enum eErrorBehavior {

    throwExceptions,
    sendSystemErrors
  }

  private eErrorBehavior errorBehavior = eErrorBehavior.sendSystemErrors;

  @Override
  public void init() {
  }

  public eErrorBehavior getErrorBehavior() {
    return errorBehavior;
  }

  public void setErrorBehavior(eErrorBehavior errorBehavior) {
    this.errorBehavior = errorBehavior;
  }

  public UserAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  public void elapseSecond() {

  }

  public void sendToPlane(String airplaneCallsignOrPart, String commands) {
    Airplane p = Airplanes.tryGetByCallsingOrNumber(Acc.planes(), airplaneCallsignOrPart);
    if (p == null) {
      raiseError(
        "Cannot identify airplane under callsign (or part) \"" + airplaneCallsignOrPart + "\" . None or multiple planes identified.");
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

  private void sendToPlane(Airplane plane, SpeechList speeches) {
    Message m = new Message(this, plane, speeches);
    super.sendMessage(m);
  }

  public void sendToAtc(Atc.eType type, String sqwkAsString) {
    Squawk s;

    s = Squawk.tryCreate(sqwkAsString);
    if (s == null) {
      raiseError("\"" + sqwkAsString + "\" is not valid transponder code.");
      return;
    }
    sendToAtc(type, s);
  }

  public void sendToAtc(Atc.eType type, Squawk squawk) {
    Airplane pln = Airplanes.tryGetBySqwk(Acc.planes(), squawk);
    if (pln == null) {
      raiseError("No such plane with callsign " + squawk.toString());
    }
    sendToAtc(type, pln);
  }

  private void sendToAtc(Atc.eType type, Airplane plane) {
    Atc atc = Acc.atc(type);

    if (getPrm().isToSwitch(plane)) {
      // je to ... -> APP

      if (getPrm().getResponsibleAtc(plane).getType() != type) {
        // nesedi smer potvrzeni A predava na APP, ale APP potvrzuje na B
        sendError("SQWK " + plane.getSqwk() + " not in your control. You cannot request switch.");
        return;
      } else {
        // potvrdime
        getPrm().confirmSwitch(this, plane);
      }
    } else {
      // je nova zadost APP -> ???
      getPrm().requestSwitch(this, atc, plane);
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

}
