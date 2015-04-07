/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.Airplanes;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.commands.CommandList;
import jatcsimlib.commands.formatting.Parser;
import jatcsimlib.commands.formatting.ShortParser;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.messaging.StringMessage;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {
  
  private Parser parser = new ShortParser();

  private void raiseError(String string) {
    switch (this.errorBehavior) {
      case sendSystemErrors:
        sendError(string);
        break;
      case throwExceptions:
        throw new ERuntimeException(string);
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
  public void init(){}
  
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

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet.");
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

    CommandList cmdList;
    try {
      cmdList = parser.parseMulti(commands);
    } catch (Exception ex) {
      raiseError(ex.getMessage());
      return;
    }
    sendToPlane(p, cmdList);
  }

  public void sendToPlane(Callsign c, CommandList commands) {
    Airplane pln = Airplanes.tryGetByCallsign(Acc.planes(), c);
    if (pln == null) {
      raiseError("No such plane for callsign \"" + c.toString() + "\".");
      return;
    }
    sendToPlane(pln, commands);
  }

  private void sendToPlane(Airplane plane, CommandList commands) {
    Acc.messenger().addMessage(Message.create(this, plane, commands));
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
    Acc.messenger().addMessage(Message.create(this, atc, msg));
  }

  public void sendError(String message) {
    Acc.messenger().addMessage(Message.createFromSystem(this, message));
  }

  public void sendSystem(String message) {
    if (message.trim().equals("?")){
      Message m = Message.createForSystem (this, message.trim());
      Acc.messenger().addMessage(m);
    }
  }

}
