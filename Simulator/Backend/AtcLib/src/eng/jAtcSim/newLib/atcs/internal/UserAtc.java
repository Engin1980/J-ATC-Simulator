/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.Tuple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequestCancelation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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

  public void sendError(IMessageContent content) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(this.getAtcId()),
        content);
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
    //TODO Implement this: Rewrite using new idea when parsing is already done here
    throw new ToDoException("Rewrite using new idea when parsing is already done here");

//    if (message.matches("\\d{4}.*")) {
//      //TODO rewrite using custom message class
//      // it is plane switch message
//      String[] tmp = RegexUtils.extractGroups(message, "^(\\d{4})( (.+))?$");
//      Squawk s = Squawk.tryCreate(tmp[1]);
//      if (s == null) {
//        raiseError("\"" + tmp[1] + "\" is not valid transponder code.");
//        return;
//      }
//      IAirplane plane = AirplaneAcc.getAirplanes().tryGet(s);
//      if (plane == null) {
//        raiseError("SQWK " + s.toString() + " does not exist.");
//        return;
//      }
//      AtcId atcId = InternalAcc.getAtc(type).getAtcId();
//      sendPlaneSwitchMessageToAtc(atcId, plane.getCallsign(), tmp[3]);
//    } else {
//      throw new ToDoException("Rewrite using custom command/notification classes.");
    // it is different message to atc
//      try {
//        IAtc2Atc content = parser.parseAtc(message);
//        sendOtherMessageToAtc(type, content);
//      } catch (Exception ex) {
//        raiseError("\"" + message + "\" has invalid syntax as message for ATC");
//      }
//    }
  }

  public void sendPlaneSwitchMessageToAtc(AtcId atcId, Callsign callsign, String additionalMessage) {
    Atc otherAtc = InternalAcc.getAtc(atcId);
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    IAtcSpeech msg;
    Squawk sqwk = InternalAcc.getSquawkFromCallsign(callsign);

    if (prm.getResponsibleAtc(callsign).equals(this.getAtcId())) {
      // it is my plane
      if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), null)) {
        // is already under switch request?
        prm.forAtc().cancelSwitchRequest(this.getAtcId(), callsign);
        msg = new PlaneSwitchRequestCancelation(sqwk);
      } else {
        // create new switch request
        prm.forAtc().createSwitchRequest(this.getAtcId(), otherAtc.getAtcId(), callsign);
        msg = PlaneSwitchRequest.createFromUser(sqwk, null, null);
      }
    } else {
      // it is not my plane
      if (prm.forAtc().isUnderSwitchRequest(callsign, otherAtc.getAtcId(), this.getAtcId())) {
        // is under switch request to me, I am making a confirmation
        if (additionalMessage == null) {
          prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
          //TODO this is not correct, the original message should be passed somehow here
          msg = new AtcConfirmation(PlaneSwitchRequest.createFromComputer(sqwk));
        } else {
          Tuple<SwitchRoutingRequest, String> routing = decodeAdditionalRouting(
              additionalMessage, callsign);
          if (routing.getB() != null) {
            sendError(new AtcRejection(PlaneSwitchRequest.createFromComputer(sqwk), routing.getB()));
            return;
          } else
            prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), routing.getA());
          msg = new AtcConfirmation(PlaneSwitchRequest.createFromComputer(sqwk));
        }
      } else {
        // making a confirmation to non-requested switch? or probably an error
        sendError(new AtcRejection(PlaneSwitchRequest.createFromComputer(sqwk),
            "SQWK " + sqwk + " not under your control and not under a switch request."));
        return;
      }
    }

    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(otherAtc.getAtcId()),
        msg);
    super.sendMessage(m);
  }

  public void sendSystem(IMessageContent content) {
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createSystem(),
        content);
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
    //TODO remove after some verification
    try {
      InternalAcc.getPlane(c);
    } catch (Exception ex) {
      throw new EApplicationException("Messages for plane " + c + " cannot be send. Plane not found.");
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

  private void confirmAtcChangeInPlaneResponsibilityManagerIfRequired(Callsign callsign, SpeechList<IForPlaneSpeech> speeches) {
    ContactCommand cc = (ContactCommand) speeches.tryGetFirst(q -> q instanceof ContactCommand);
    if (cc != null) {
      InternalAcc.getPrm().forAtc().applyConfirmedSwitch(this.getAtcId(), callsign);
    }
  }

  private Tuple<SwitchRoutingRequest, String> decodeAdditionalRouting(String text, Callsign callsign) {
    //TODO rewrite using some smart message, to not use parsing here
    EAssert.Argument.isNotNull(callsign, "callsign");
    IAirplane plane = InternalAcc.getPlane(callsign);

    Matcher m = Pattern.compile("(\\d{1,2}[lrcLRC]?)?(\\/(.+))?").matcher(text);
    EAssert.isTrue(m.find(), sf("Unable to decode switch routing request from '%s'.", text));

    ActiveRunwayThreshold threshold;
    DARoute route;

    if (m.group(1) == null)
      threshold = plane.getRouting().getAssignedRunwayThreshold();
    else {
      threshold = Context.getArea().getAirport().tryGetRunwayThreshold(m.group(1));
      if (threshold == null) {
        return new Tuple<>(null, "Unable to find runway threshold {" + m.group(1) + "}.");
      }
    }

    if (m.group(3) == null) {
      if (threshold == plane.getRouting().getAssignedRunwayThreshold())
        route = null;
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
//    Tuple<SwitchRoutingRequest, String> ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
    if (threshold == plane.getRouting().getAssignedRunwayThreshold() && route.getName().equals(plane.getRouting().getAssignedDARouteName()))
      ret = new Tuple<>(null, null);
    else
      ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
    return ret;
  }

//  private void raiseError(String text) {
//    //TODO do not know what this is doing:
//    super.getRecorder().write("ERR", text);
//    switch (this.errorBehavior) {
//      case sendSystemErrors:
//        sendError(text);
//        break;
//      case throwExceptions:
//        throw new ERuntimeException(text);
//      default:
//        throw new UnsupportedOperationException();
//    }
//  }

//  private String[] splitToCallsignAndMessages(String msg) {
//    String[] ret = new String[2];
//    int i = msg.indexOf(" ");
//    if (i == msg.length() || i < 0) {
//      ret[0] = msg;
//      ret[1] = "";
//    } else {
//      ret[0] = msg.substring(0, i);
//      ret[1] = msg.substring(i + 1);
//    }
//    return ret;
//  }

}
