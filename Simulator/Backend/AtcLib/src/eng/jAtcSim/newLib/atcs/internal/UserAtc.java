/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.Tuple;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.*;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.atc.PlaneSwitch;

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

  //  private final Parser parser = new ShortBlockParser();
  private eErrorBehavior errorBehavior = eErrorBehavior.sendSystemErrors;

  public UserAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  public void elapseSecond() {
  }

  public eErrorBehavior getErrorBehavior() {
    return errorBehavior;
  }

  public void setErrorBehavior(eErrorBehavior errorBehavior) {
    this.errorBehavior = errorBehavior;
  }

//  public Parser getParser() {
//    return parser;
//  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  @Override
  public void registerNewPlaneUnderControl(Callsign callsign, boolean finalRegistration) {

  }

  @Override
  public void removePlaneDeletedFromGame(Callsign callsign) {

  }

  public void sendError(String message) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(this.getAtcId()),
        new StringMessageContent(message));
    super.sendMessage(m);
  }

  public void sendOtherMessageToAtc(AtcId recieverAtcId, IMessageContent msg) {
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(recieverAtcId),
        msg);
    super.sendMessage(m);
  }

  public void sendPlaneSwitchMessageToAtc(AtcType type, String message) {
    if (message.matches("\\d{4}.*")) {
      //TODO rewrite using custom message class
      // it is plane switch message
      String[] tmp = RegexUtils.extractGroups(message, "^(\\d{4})( (.+))?$");
      Squawk s = Squawk.tryCreate(tmp[1]);
      if (s == null) {
        raiseError("\"" + tmp[1] + "\" is not valid transponder code.");
        return;
      }
      IAirplane plane = AirplaneAcc.getAirplanes().tryGet(s);
      if (plane == null) {
        raiseError("SQWK " + s.toString() + " does not exist.");
        return;
      }
      AtcId atcId = InternalAcc.getAtc(type).getAtcId();
      sendPlaneSwitchMessageToAtc(atcId, plane.getCallsign(), tmp[3]);
    } else {
      throw new ToDoException("Rewrite using custom command/notification classes.");
      // it is different message to atc
//      try {
//        IAtc2Atc content = parser.parseAtc(message);
//        sendOtherMessageToAtc(type, content);
//      } catch (Exception ex) {
//        raiseError("\"" + message + "\" has invalid syntax as message for ATC");
//      }
    }
  }

  public void sendPlaneSwitchMessageToAtc(AtcId atcId, Callsign callsign, String additionalMessage) {
    Atc otherAtc = InternalAcc.getAtc(atcId);
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    PlaneSwitch.eMessageType msgType;

    if (prm.getResponsibleAtc(callsign).equals(this.getAtcId())) {
      // it is my plane
      if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), null)) {
        // is already under switch request?
        prm.forAtc().cancelSwitchRequest(this.getAtcId(), callsign);
        msgType = PlaneSwitch.eMessageType.cancelation;
      } else {
        // create new switch request
        prm.forAtc().createSwitchRequest(this.getAtcId(), otherAtc.getAtcId(), callsign);
        msgType = PlaneSwitch.eMessageType.request;
      }
    } else {
      // it is not my plane
      if (prm.forAtc().isUnderSwitchRequest(callsign, otherAtc.getAtcId(), this.getAtcId())) {
        // is under switch request to me, I am making a confirmation
        if (additionalMessage == null) {
          prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
          msgType = PlaneSwitch.eMessageType.confirmation;
        } else {
          Tuple<SwitchRoutingRequest, String> routing = decodeAdditionalRouting(
              additionalMessage, callsign);
          if (routing.getB() != null) {
            sendError(routing.getB());
            return;
          } else
            prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), routing.getA());
          msgType = PlaneSwitch.eMessageType.confirmation;
        }
      } else {
        // making a confirmation to non-requested switch? or probably an error
        IAirplane plane = InternalAcc.getPlane(callsign);
        sendError("SQWK " + plane.getSqwk() + " not under your control and not under a switch request.");
        return;
      }
    }

    PlaneSwitch msg = new PlaneSwitch(callsign, msgType);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(otherAtc.getAtcId()),
        msg);
    super.sendMessage(m);
  }

  public void sendSystem(String message) {
    if (message.trim().isEmpty()) {
      message = "?";
    }
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createSystem(),
        new StringMessageContent(message.trim()));
    super.sendMessage(m);
  }

  public void sendToPlane(String line) {
    throw new ToDoException("This definitely must be somewhere else.");
//    String[] tmp = splitToCallsignAndMessages((line));
//    String airplaneCallsignOrPart = tmp[0];
//    String commands = tmp[1];
//
//    Airplane p = Airplanes.tryGetByCallsingOrNumber(Acc.planes(), airplaneCallsignOrPart);
//    if (p == null) {
//      raiseError(
//          "Cannot identify airplane under callsign (or part) \"" + airplaneCallsignOrPart + "\". None or multiple planes identified.");
//      return;
//    }
//
//    SpeechList cmdList;
//    try {
//      cmdList = parser.parseMulti(commands);
//    } catch (Exception ex) {
//      raiseError(ex.getMessage());
//      return;
//    }
//    sendToPlane(p, cmdList);
  }

  public void sendToPlane(Callsign c, SpeechList speeches) {
    if (AirplaneAcc.getAirplanes().tryGet(c) == null) {
      raiseError("No such plane for callsign \"" + c.toString() + "\".");
      return;
    }

    confirmAtcChangeInPlaneResponsibilityManagerIfRequired(c, speeches);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAirplane(c),
        speeches);
    super.sendMessage(m);
  }

  @Override
  public void unregisterPlaneUnderControl(Callsign callsign) {

  }

//  @Override
//  protected void _load(XElement elm) {
//
//  }
//
//  @Override
//  protected void _save(XElement elm) {
//
//  }

  private void confirmAtcChangeInPlaneResponsibilityManagerIfRequired(Callsign callsign, SpeechList speeches) {
    ContactCommand cc = (ContactCommand) speeches.tryGetFirst(q -> q instanceof ContactCommand);
    if (cc != null) {
      InternalAcc.getPrm().forAtc().applyConfirmedSwitch(this.getAtcId(), callsign);
    }
  }

  private Tuple<SwitchRoutingRequest, String> decodeAdditionalRouting(String text, Callsign callsign) {
    //TODO rewrite using some smart message, to not use parsing here
    EAssert.Argument.isNotNull(callsign, "callsign");
    IAirplane plane = InternalAcc.getPlane(callsign);

    Matcher m =
        Pattern.compile("(\\d{1,2}[lrcLRC]?)?(\\/(.+))?")
            .matcher(text);
    boolean found = m.find();
    assert found;
    ActiveRunwayThreshold threshold;
    if (m.group(1) == null)
      threshold = plane.getRouting().getAssignedRunwayThreshold();
    else {
      threshold = AreaAcc.getAirport().tryGetRunwayThreshold(m.group(1));
      if (threshold == null) {
        return new Tuple<>(null, "Unable to find runway threshold {" + m.group(1) + "}.");
      }
    }

    DARoute route;
    if (m.group(3) == null) {
      if (threshold == plane.getRouting().getAssignedRunwayThreshold())
        route = plane.getRouting().getAssignedRoute();
      else {
        throw new ToDoException("Implement this");
//        route = plane.isArrival()
//            ? threshold.getArrivalRouteForPlane(plane.getType(), plane.getSha().getTargetAltitude(), plane.getRouting().getEntryExitPoint(), true)
//            : threshold.getDepartureRouteForPlane(plane.getType(), plane.getRouting().getEntryExitPoint(), true);
      }
    } else if (m.group(3).toUpperCase().equals("V")) {
      route = DARoute.createNewVectoringByFix(plane.getRouting().getEntryExitPoint());
    } else {
      route = threshold.getRoutes().tryGetFirst(q -> q.getName().equals(m.group(3)));
      if (route == null)
        return new Tuple<>(null, "Unable to find route {" + m.group((3) + "} for runway threshold {" + threshold.getName() + "}."));
    }

    Tuple<SwitchRoutingRequest, String> ret;
    if (threshold == plane.getRouting().getAssignedRunwayThreshold() && route == plane.getRouting().getAssignedRoute())
      ret = new Tuple<>(null, null);
    else
      ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
    return ret;
  }

  private void raiseError(String text) {
    //TODO do not know what this is doing:
    super.getRecorder().write( "ERR", text);
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

}
