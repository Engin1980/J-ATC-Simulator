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
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.newXmlUtils.annotations.XmlConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class UserAtc extends Atc implements IUserAtcInterface {

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

  private final IList<IAirplane> planes = new EList<>();
  private IReadOnlyList<Message> thisSecondMessages = new EList<>();

  @XmlConstructor
  private UserAtc() {
  }

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
    RadarContactConfirmationNotification rccn = (RadarContactConfirmationNotification) cmds.tryGetFirst(q -> q instanceof RadarContactConfirmationNotification);
    if (rccn != null) {
      IAirplane plane = Context.Internal.getPlane(toCallsign);
      if (this.planes.contains(plane) == false)
        this.planes.add(plane);
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

  //TODEL
  private void processIncomingGoodDayFromPlane(IAirplane plane) {
    // intentionally blank
  }

  private void processOutgoingPlaneSwitchMessage(AtcId toAtcId, IAtcSpeech atcSpeech) {
    PlaneSwitchRequest psr = tryGetBaseIfBasedOnPlaneSwitch(atcSpeech);
    EAssert.isNotNull(psr);
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
}
