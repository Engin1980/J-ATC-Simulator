/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.eSystem.Tuple;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.*;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRO;
import eng.jAtcSim.lib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

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
    IAirplaneRO pln = Airplanes.tryGetByCallsign(Acc.planes(), c);
    if (pln == null) {
      raiseError("No such plane for callsign \"" + c.toString() + "\".");
      return;
    }
    sendToPlane(pln, speeches);
  }

  public void sendPlaneSwitchMessageToAtc(Atc.eType type, String message) {
    if (message.matches("\\d{4}.*")) {
      // it is plane switch message
      String[] tmp = RegexUtils.extractGroups(message, "^(\\d{4})( (.+))?$");
      Squawk s = Squawk.tryCreate(tmp[1]);
      if (s == null) {
        raiseError("\"" + tmp[1] + "\" is not valid transponder code.");
        return;
      }
      Airplane plane = Airplanes.tryGetBySqwk(Acc.planes(), s);
      if (plane == null) {
        raiseError("SQWK " + s.toString() + " does not exist.");
        return;
      }
      sendPlaneSwitchMessageToAtc(type, plane, tmp[3]);
    } else {
      // it is different message to atc
      try {
        IAtc2Atc content = parser.parseAtc(message);
        sendOtherMessageToAtc(type, content);
      } catch (Exception ex) {
        raiseError("\"" + message + "\" has invalid syntax as message for ATC");
      }
    }
  }

  public void sendOtherMessageToAtc(Atc.eType type, IAtc2Atc msg) {
    Atc atc = Acc.atc(type);
    Message m = new Message(this, atc, msg);
    super.sendMessage(m);
  }

  public void sendPlaneSwitchMessageToAtc(Atc.eType type, IAirplaneRO plane, String additionalMessage) {
    Atc otherAtc = Acc.atc(type);
    PlaneSwitchMessage.eMessageType msgType;

    if (getPrm().getResponsibleAtc(plane) == this) {
      // it is my plane
      if (getPrm().isUnderSwitchRequest(plane, this, null)) {
        // is already under switch request?
        getPrm().cancelSwitchRequest(this, plane);
        msgType = PlaneSwitchMessage.eMessageType.cancelation;
      } else {
        // create new switch request
        getPrm().createSwitchRequest(this, otherAtc, plane);
        msgType = PlaneSwitchMessage.eMessageType.request;
      }
    } else {
      // it is not my plane
      if (getPrm().isUnderSwitchRequest(plane, otherAtc, this)) {
        // is under switch request to me, I am making a confirmation
        if (additionalMessage == null) {
          getPrm().confirmSwitchRequest(plane, this, null);
          msgType = PlaneSwitchMessage.eMessageType.confirmation;
        }
        else {
          Tuple<SwitchRoutingRequest, String> routing = decodeAdditionalRouting(
              additionalMessage, plane);
          if (routing.getB() != null) {
            sendError(routing.getB());
            return;
          } else
            getPrm().confirmSwitchRequest(plane, this, routing.getA());
          msgType = PlaneSwitchMessage.eMessageType.confirmation;
        }
      } else {
        // making a confirmation to non-requested switch? or probably an error
        sendError("SQWK " + plane.getSqwk() + " not under your control and not under a switch request.");
        return;
      }
    }

    PlaneSwitchMessage msg = new PlaneSwitchMessage(plane, msgType);
    Message m = new Message(this, otherAtc, msg);
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

  private Tuple<SwitchRoutingRequest, String> decodeAdditionalRouting(String text, IAirplaneRO plane) {
    Validator.isNotNull(plane);

    Matcher m =
        Pattern.compile("(\\d{1,2}[lrcLRC]?)?(\\/(.+))?")
            .matcher(text);
    boolean found =  m.find();
    assert found;
    ActiveRunwayThreshold threshold;
    if (m.group(1) == null)
      threshold = plane.getRoutingModule().getAssignedRunwayThreshold();
    else {
      threshold = Acc.airport().tryGetRunwayThreshold(m.group(1));
      if (threshold == null) {
        return new Tuple<>(null, "Unable to find runway threshold {" + m.group(1) + "}.");
      }
    }

    Route route;
    if (m.group(3) == null) {
      if (threshold == plane.getRoutingModule().getAssignedRunwayThreshold())
        route = plane.getRoutingModule().getAssignedRoute();
      else
        route = plane.getFlightModule().isArrival()
            ? threshold.getArrivalRouteForPlane(plane.getType(), plane.getSha().getTargetAltitude(), plane.getRoutingModule().getEntryExitPoint(), true)
            : threshold.getDepartureRouteForPlane(plane.getType(), plane.getRoutingModule().getEntryExitPoint(), true);
    } else if (m.group(3).toUpperCase().equals("V")) {
      route = Route.createNewVectoringByFix(plane.getRoutingModule().getEntryExitPoint());
    } else {
      route = threshold.getRoutes().tryGetFirst(q -> q.getName().equals(m.group(3)));
      if (route == null)
        return new Tuple<>(null, "Unable to find route {" + m.group((3) + "} for runway threshold {" + threshold.getName() + "}."));
    }

    Tuple<SwitchRoutingRequest, String> ret;
    if (threshold == plane.getRoutingModule().getAssignedRunwayThreshold() && route == plane.getRoutingModule().getAssignedRoute())
      ret = new Tuple<>(null, null);
    else
      ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
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
  public void unregisterPlaneUnderControl(IAirplaneRO plane) {

  }

  @Override
  public void removePlaneDeletedFromGame(IAirplaneRO plane) {

  }

  @Override
  public void registerNewPlaneUnderControl(IAirplaneRO plane, boolean finalRegistration) {

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

  private void sendToPlane(IAirplaneRO plane, SpeechList speeches) {
    confirmAtcChangeInPlaneResponsibilityManagerIfRequired(plane, speeches);
    Message m = new Message(this, plane, speeches);
    super.sendMessage(m);
  }

  private void confirmAtcChangeInPlaneResponsibilityManagerIfRequired(IAirplaneRO plane, SpeechList speeches) {
    ContactCommand cc = (ContactCommand) speeches.tryGetFirst(q -> q instanceof ContactCommand);
    if (cc != null) {
      getPrm().applyConfirmedSwitch(this, plane);
    }
  }

}
