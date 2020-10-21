package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  private final DelayedList<Message> speechDelayer = new DelayedList<>(
          Global.MINIMUM_ATC_SPEECH_DELAY_SECONDS, Global.MAXIMUM_ATC_SPEECH_DELAY_SECONDS);
  private final SwitchManager switchManager = new SwitchManager(this,
          m -> sendMessage(m),
          () -> speechDelayer.getAll());

  public ComputerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  protected abstract void processConfirmedOutgoingPlaneSwitch(Squawk squawk);

  protected abstract boolean acceptsNewRouting(IAirplane airplane, PlaneSwitchRequestRouting routing);

  protected abstract RequestResult canIAcceptPlaneIncomingFromAnotherAtc(IAirplane plane);

  protected abstract boolean isPlaneReadyToSwitchToAnotherAtc(IAirplane airplane);

  protected abstract AtcId getAtcIdWhereIAmSwitchingPlanes();

  protected abstract void processMessagesFromPlane(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs);

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract IReadOnlyList<IAirplane> getPlanesUnderControl();

  @Override
  public final void elapseSecond() {
    IList<Message> thisSecondMsgs = super.pullMessages();
    speechDelayer.add(thisSecondMsgs);

    IList<Message> msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    this.switchManager.elapseSecond();

    _elapseSecond();
  }

  protected abstract void _elapseSecond();

  @Override
  public void init() {
    Context.getMessaging().getMessenger().registerListener(
            Participant.createAtc(this.getAtcId()));
  }

  @Override
  public boolean isHuman() {
    return false;
  }

  @Override
  public boolean isResponsibleFor(Callsign callsign) {
    boolean ret = this.getPlanesUnderControl().isAny(q -> q.getCallsign().equals(callsign));
    return ret;
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    EAssert.Argument.isTrue(m.getSource().getType() == Participant.eType.atc);
    EAssert.Argument.isFalse(
            m.getContent() instanceof PlaneConfirmation && ((PlaneConfirmation) m.getContent()).getOrigin() instanceof PlaneSwitchRequest,
            "Plane-Confirmation is not supported for Plane-Switch-Request message type.");
    EAssert.Argument.isFalse(
            m.getContent() instanceof PlaneRejection && ((PlaneRejection) m.getContent()).getOrigin() instanceof PlaneSwitchRequest,
            "Plane-Rejection is not supported for Plane-Switch-Request message type.");


    AtcId sender = Context.Internal.getAtcId(m.getSource().getId());

    if (m.getContent() instanceof PlaneSwitchRequest){
      this.switchManager.processPlaneSwitchMessage((PlaneSwitchRequest) m.getContent(), sender);
    } else {
      processNonPlaneSwitchMessageFromAtc(m);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        if (m.getSource().getType() == Participant.eType.airplane)
          elapseSecondProcessMessagesFromPlane(m);
        else if (m.getSource().getType() == Participant.eType.atc)
          elapseSecondProcessMessageFromAtc(m);
      } catch (Exception ex) {
        throw new EApplicationException(sf(
                "Failed to process a message for Atc. Atc: %s. Message from %s. Message itself: %s.",
                this.getAtcId().getName(),
                m.getSource().getId(),
                m.toString()), ex);
      }
    }
  }

  private void elapseSecondProcessMessagesFromPlane(Message m) {
    Callsign callsign = new Callsign(m.getSource().getId());
    IAirplane plane = Context.Internal.getPlane(callsign);
    SpeechList<IFromPlaneSpeech> spchs = m.getContent();

    IList<GoodDayNotification> goodDayNotifications = spchs
            .whereItemClassIs(GoodDayNotification.class, false)
            .where(q->q.isRepeated() == false);
    EAssert.isTrue(goodDayNotifications.isAll(q->q.getCallsign().equals(plane.getCallsign())));
    goodDayNotifications.forEach(q->this.switchManager.processGoodDayFromPlane(q));

    processMessagesFromPlane(plane, spchs);
  }

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }
}
