/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.IUserAtcInterface;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class UserAtc extends Atc implements IUserAtcInterface {

  private final IList<IAirplane> planes = new EList<>();
  private IReadOnlyList<Message> thisSecondMessages = new EList<>();

  public UserAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  @Override
  public void elapseSecond() {
    this.thisSecondMessages = Context.getMessaging().getMessenger().getMessagesByListener(Participant.createAtc(this.getAtcId()), true);
    for (Message message : this.thisSecondMessages) {
      if (message.getSource().getType() == Participant.eType.airplane) {
        SpeechList<IPlaneSpeech> speechList = message.getContent();
        if (speechList.isEmpty() == false && speechList.getFirst() instanceof GoodDayNotification) {
          Callsign callsign = new Callsign(message.getSource().getId());
          IAirplane plane = Context.Internal.getPlane(callsign);
          processIncomingGoodDayFromPlane(plane);
        }
      }
    }
  }

  public IReadOnlyList<Message> getNewMessages() {
    return thisSecondMessages;
  }

  @Override
  public void init() {
    Context.getMessaging().getMessenger().registerListener(
            Participant.createAtc(this.getAtcId()));
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  @Override
  public boolean isResponsibleFor(Callsign callsign) {
    return this.planes.isAny(q -> q.getCallsign().equals(callsign));
  }

  @Override
  public void registerNewPlaneInGame(Callsign callsign, boolean finalRegistration) {
    throw new UnsupportedOperationException(sf("This operation is not supported by UserAtc %s.", getAtcId()));
  }

  @Override
  public void sendAtcCommand(AtcId toAtcId, IAtcSpeech atcSpeech) {
    PlaneSwitchRequest psr = tryGetBaseIfBasedOnPlaneSwitch(atcSpeech);
    if (psr != null) {
      processOutgoingPlaneSwitchMessage(toAtcId, atcSpeech);
    } else {
      eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAtc(toAtcId),
              atcSpeech
      );
      super.sendMessage(msg);
    }
  }

  @Override
  public void sendPlaneCommand(Callsign toCallsign, SpeechList<IForPlaneSpeech> cmds) {
    ContactCommand cc = (ContactCommand) cmds.tryGetFirst(q -> q instanceof ContactCommand);
    if (cc != null) {
      processOutgoingContactCommandToPlane(toCallsign, cc);
    }
    eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAirplane(toCallsign),
            cmds
    );
    super.sendMessage(msg);
  }

  @Override
  public void sendSystemCommand(ISystemSpeech systemSpeech) {
    eng.jAtcSim.newLib.messaging.Message msg = new eng.jAtcSim.newLib.messaging.Message(
            Participant.createAtc(getAtcId()),
            Participant.createSystem(),
            systemSpeech
    );
    super.sendMessage(msg);
  }

  @Override
  public void unregisterPlaneDeletedFromGame(Callsign callsign, boolean isForcedDeletion) {
    IAirplane plane = Context.Internal.getPlane(callsign);
    this.planes.tryRemove(plane);
  }

  private void processIncomingGoodDayFromPlane(IAirplane plane) {
    this.planes.add(plane);
  }

  private void processOutgoingPlaneSwitchMessage(AtcId toAtcId, IAtcSpeech atcSpeech) {
    PlaneSwitchRequest psr = tryGetBaseIfBasedOnPlaneSwitch(atcSpeech);
    EAssert.isNotNull(psr);
    IAirplane plane = this.planes.tryGetFirst(q -> q.getSqwk().equals(psr.getSquawk()));
    if (plane != null) {
      // sending new switch request, but not having the plane
      super.sendMessage(
              new Message(
                      Participant.createSystem(),
                      Participant.createAtc(this.getAtcId()),
                      new AtcRejection(atcSpeech, sf("Squawk '%s' not found.", psr.getSquawk()))));
    } else
      super.sendMessage(new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAtc(toAtcId),
              atcSpeech
      ));
  }

  private void processOutgoingContactCommandToPlane(Callsign toCallsign, ContactCommand contactCommand) {
    IAirplane plane = Context.Internal.getPlane(toCallsign);
    this.planes.remove(plane);
  }

  private static PlaneSwitchRequest tryGetBaseIfBasedOnPlaneSwitch(IAtcSpeech speech) {
    //TODO duplicit with the same method in ComputerAtc, how to solve this?
    if (speech instanceof PlaneSwitchRequest)
      return (PlaneSwitchRequest) speech;
    else if (speech instanceof AtcConfirmation)
      return (PlaneSwitchRequest) ((AtcConfirmation) speech).getOrigin();
    else if (speech instanceof AtcRejection)
      return (PlaneSwitchRequest) ((AtcRejection) speech).getOrigin();
    else
      return null;
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

//  private void confirmAtcChangeInPlaneResponsibilityManagerIfRequired(IAirplane plane, SpeechList<IForPlaneSpeech> speeches) {
//    ContactCommand cc = (ContactCommand) speeches.tryGetFirst(q -> q instanceof ContactCommand);
//    if (cc != null)
//      Context.Internal.getPre().setResponsibleAtc(plane.getSqwk(), this.getAtcId());
//  }


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

  // TODEL
//  private void processOutgoingPlaneSwitchMessage(AtcId atcId, Callsign callsign, String additionalMessage) {
//
//    IAtcSpeech msg;
//    Squawk sqwk = Context.Internal.getPlane(callsign).getSqwk();
//    if (this.planes.isAny(q -> q.getCallsign().equals(callsign)))
//
//      if (pre.getResponsibleAtc(sqwk).equals(this.getAtcId())) {
//        // it is my plane
//        if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), null)) {
//          // is already under switch request?
//          prm.forAtc().cancelSwitchRequest(this.getAtcId(), callsign);
//          msg = new PlaneSwitchRequestCancelation(sqwk);
//        } else {
//          // create new switch request
//          prm.forAtc().createSwitchRequest(this.getAtcId(), otherAtc.getAtcId(), callsign);
//          msg = PlaneSwitchRequest.createFromUser(sqwk, null, null);
//        }
//      } else {
//        // it is not my plane
//        if (prm.forAtc().isUnderSwitchRequest(callsign, otherAtc.getAtcId(), this.getAtcId())) {
//          // is under switch request to me, I am making a confirmation
//          if (additionalMessage == null) {
//            prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
//            //TODO this is not correct, the original message should be passed somehow here
//            msg = new AtcConfirmation(PlaneSwitchRequest.createFromComputer(sqwk));
//          } else {
//            Tuple<SwitchRoutingRequest, String> routing = decodeAdditionalRouting(
//                    additionalMessage, callsign);
//            if (routing.getB() != null) {
//              sendError(new AtcRejection(PlaneSwitchRequest.createFromComputer(sqwk), routing.getB()));
//              return;
//            } else
//              prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), routing.getA());
//            msg = new AtcConfirmation(PlaneSwitchRequest.createFromComputer(sqwk));
//          }
//        } else {
//          // making a confirmation to non-requested switch? or probably an error
//          sendError(new AtcRejection(PlaneSwitchRequest.createFromComputer(sqwk),
//                  "SQWK " + sqwk + " not under your control and not under a switch request."));
//          return;
//        }
//      }
//
//    Message m = new Message(
//            Participant.createAtc(this.getAtcId()),
//            Participant.createAtc(otherAtc.getAtcId()),
//            msg);
//    super.sendMessage(m);
//  }

  //TODEL delete when unused
//  public void sendToPlane(String line) {
//    throw new ToDoException("This definitely must be somewhere else.");
////    String[] tmp = splitToCallsignAndMessages((line));
////    String airplaneCallsignOrPart = tmp[0];
////    String commands = tmp[1];
////
////    Airplane p = Airplanes.tryGetByCallsingOrNumber(Acc.planes(), airplaneCallsignOrPart);
////    if (p == null) {
////      raiseError(
////          "Cannot identify airplane under callsign (or part) \"" + airplaneCallsignOrPart + "\". None or multiple planes identified.");
////      return;
////    }
////
////    SpeechList cmdList;
////    try {
////      cmdList = parser.parseMulti(commands);
////    } catch (Exception ex) {
////      raiseError(ex.getMessage());
////      return;
////    }
////    sendToPlane(p, cmdList);
//  }

  //TODEL delete when unused
//  public void sendToPlane(Callsign c, SpeechList speeches) {
//    //TODO remove after some verification
//    try {
//      Context.Internal.getPlane(c);
//    } catch (Exception ex) {
//      throw new EApplicationException("Messages for plane " + c + " cannot be send. Plane not found.");
//    }
//
//    confirmAtcChangeInPlaneResponsibilityManagerIfRequired(c, speeches);
//    Message m = new Message(
//            Participant.createAtc(this.getAtcId()),
//            Participant.createAirplane(c),
//            speeches);
//    super.sendMessage(m);
//  }


  //TODO delete this when unused
//  public void sendPlaneSwitchMessageToAtc(AtcType type, String message) {
//    //TODO Implement this: Rewrite using new idea when parsing is already done here
//    throw new ToDoException("Rewrite using new idea when parsing is already done here");
//
////    if (message.matches("\\d{4}.*")) {
////      //TODO rewrite using custom message class
////      // it is plane switch message
////      String[] tmp = RegexUtils.extractGroups(message, "^(\\d{4})( (.+))?$");
////      Squawk s = Squawk.tryCreate(tmp[1]);
////      if (s == null) {
////        raiseError("\"" + tmp[1] + "\" is not valid transponder code.");
////        return;
////      }
////      IAirplane plane = AirplaneAcc.getAirplanes().tryGet(s);
////      if (plane == null) {
////        raiseError("SQWK " + s.toString() + " does not exist.");
////        return;
////      }
////      AtcId atcId = InternalAcc.getAtc(type).getAtcId();
////      sendPlaneSwitchMessageToAtc(atcId, plane.getCallsign(), tmp[3]);
////    } else {
////      throw new ToDoException("Rewrite using custom command/notification classes.");
//    // it is different message to atc
////      try {
////        IAtc2Atc content = parser.parseAtc(message);
////        sendOtherMessageToAtc(type, content);
////      } catch (Exception ex) {
////        raiseError("\"" + message + "\" has invalid syntax as message for ATC");
////      }
////    }
//  }

  //TODEL
//  public void sendError(IMessageContent content) {
//    Message m = new Message(
//            Participant.createSystem(),
//            Participant.createAtc(this.getAtcId()),
//            content);
//    super.sendMessage(m);
//  }

//TODEL
//  public void sendOtherMessageToAtc(AtcId recieverAtcId, IMessageContent msg) {
//    Message m = new Message(
//            Participant.createAtc(this.getAtcId()),
//            Participant.createAtc(recieverAtcId),
//            msg);
//    super.sendMessage(m);
//  }

}
