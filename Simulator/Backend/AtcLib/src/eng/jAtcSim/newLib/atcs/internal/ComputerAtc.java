package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchInfo;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  protected final IList<SwitchInfo> outgoingPlanes = new EList<>();
  protected final IList<SwitchInfo> incomingPlanes = new EList<>();
  protected final IList<IAirplane> planes = new EList<>();
  private final SwitchManager switchManager = new SwitchManager();

  private class SwitchManager{

    private final ComputerAtc parent = ComputerAtc.this;

    private void checkAndProcessPlanesReadyToSwitch() {
      for (IAirplane airplane : parent.planes) {
        if (parent.outgoingPlanes.isAny(q->q.equals(airplane)))
          continue;

        AtcId targetAtcId = parent.getTargetAtcIfPlaneIsReadyToSwitch(airplane);
        if (targetAtcId != null) {
          this.requestNewSwitch(airplane, targetAtcId);
        }
      }
    }

    private void requestNewSwitch(IAirplane airplane, AtcId targetAtcId) {
      SwitchInfo si = new SwitchInfo(airplane, targetAtcId, Context.getShared().getNow().toStamp());
      parent.outgoingPlanes.add(si);
      Message m = new Message(
          Participant.createAtc(parent.getAtcId()),
          Participant.createAtc(targetAtcId),
          PlaneSwitchRequest.createFromComputer(airplane.getSqwk(), false));
      sendMessage(m);
    }

    private static final int SECONDS_BEFORE_REPEAT_SWITCH_REQUEST = 30;

    private void repeatOldSwitchRequests() {
      EDayTimeRun now = Context.getShared().getNow();
      IReadOnlyList<SwitchInfo> awaitings = this.parent.outgoingPlanes.where(
          q->q.getTime().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

      for (SwitchInfo si : awaitings) {
        IAirplane airplane = si.getAirplane();
        if (speechDelayer
            .isAny(q -> q.getContent() instanceof PlaneSwitchRequest && ((PlaneSwitchRequest) q.getContent()).getSquawk().equals(airplane.getCallsign())))
          continue; // if message about this plane is delayed and waiting to process
        Squawk sqwk = airplane.getSqwk();
        Message m = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(si.getAtcId()),
            PlaneSwitchRequest.createFromComputer(sqwk, true));
        Context.getMessaging().getMessenger().send(m);
        si.updateTime(now.toStamp());
        parent.getRecorder().write(m);
      }
    }

    public void elapseSecond() {
      this.checkAndProcessPlanesReadyToSwitch();
      this.repeatOldSwitchRequests();
    }


    public void acceptSwitch(Callsign callsign, AtcId targetAtcId, PlaneSwitchRequest psr) {
      PlaneResponsibilityManager prm = InternalAcc.getPrm();
      prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
      Message nm = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAtc(targetAtcId),
          new AtcConfirmation(psr));
      sendMessage(nm);
    }


    private void applyConfirmedSwitch(Squawk squawk) {
      PlaneResponsibilityManager prm = InternalAcc.getPrm();
      prm.forAtc().applyConfirmedSwitch(this.getAtcId(), callsign);
      AtcId newTargetAtc = prm.getResponsibleAtc(callsign);
      Message msg = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAirplane(callsign),
          new SpeechList<>(
              new ContactCommand(newTargetAtc)));
      Context.getMessaging().getMessenger().send(msg);
    }
  }

  public static class RequestResult {
    public final boolean isAccepted;
    public final String message;

    public RequestResult(boolean isAccepted, String message) {
      this.isAccepted = isAccepted;
      this.message = message;
    }
  }

  private final DelayedList<Message> speechDelayer = new DelayedList<>(
      Global.MINIMUM_ATC_SPEECH_DELAY_SECONDS, Global.MAXIMUM_ATC_SPEECH_DELAY_SECONDS);

  public ComputerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  public void elapseSecond() {

    IList<Message> msgs = Context.getMessaging().getMessenger().getMessagesByListener(
        Participant.createAtc(this.getAtcId()), true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    this.switchManager.elapseSecond();

  }

  @Override
  public void init() {
    Context.getMessaging().getMessenger().registerListener(
        Participant.createAtc(this.getAtcId()));
  }

  @Override
  public boolean isHuman() {
    return false;
  }

  protected abstract boolean acceptsNewRouting(Callsign callsign, SwitchRoutingRequest srr);

  protected abstract RequestResult canIAcceptPlane(Callsign callsign);

  /**
   * Returns target atc if plane is ready for switch.
   *
   * @param callsign Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  protected abstract AtcId getTargetAtcIfPlaneIsReadyToSwitch(IAirplane airplane);

  protected abstract void processMessagesFromPlane(Callsign callsign, SpeechList<IFromPlaneSpeech> spchs);

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);


  protected abstract boolean shouldBeSwitched(Callsign plane);

  private void confirmGoodDayNotificationIfRequired(Callsign callsign, SpeechList<IFromPlaneSpeech> spchs) {
    IList<GoodDayNotification> gdns = spchs.whereItemClassIs(GoodDayNotification.class, false);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
      lst.add(new RadarContactConfirmationNotification());
      if (InternalAcc.getPrm().forAtc().getResponsibleAtc(callsign).equals(this.getAtcId())) {
        AtcId atcId = InternalAcc.getAtc(AtcType.app).getAtcId();
        lst.add(new ContactCommand(atcId));
      }
      Message msg = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAirplane(callsign),
          lst);
      sendMessage(msg);
    }
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    if (m.getContent() instanceof PlaneSwitchRequest) {
      processPlaneSwitchMessage(m);
    } else {
      processNonPlaneSwitchMessageFromAtc(m);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        super.getRecorder().write(m); // incoming item

        if (m.getSource().getType() == Participant.eType.airplane) {
          // messages from planes
          Callsign callsign = new Callsign(m.getSource().getId());
          SpeechList<IFromPlaneSpeech> spchs = m.getContent();

          if (spchs.containsType(GoodDayNotification.class))
            confirmGoodDayNotificationIfRequired(callsign, spchs);
          processMessagesFromPlane(callsign, spchs);
        } else if (m.getSource().getType() == Participant.eType.atc) {
          elapseSecondProcessMessageFromAtc(m);
        }
      } catch (Exception ex) {
        throw new EApplicationException(sf(
            "Failed to process a message for Atc. Atc: %s. Message from %s. Message itself: %s.",
            this.getAtcId().getName(),
            m.getSource().getId(),
            m.toString()), ex);
      }
    }
  }

  private void processPlaneSwitchMessage(Message m) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    PlaneSwitchRequest psm = m.getContent();
    Callsign callsign = InternalAcc.getCallsignFromSquawk(psm.getSquawk());
    EAssert.isTrue(m.getSource().getType() == Participant.eType.atc);
    AtcId targetAtcId = InternalAcc.getAtc(m.getSource().getId()).getAtcId();
    if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), targetAtcId)) {
      // other ATC confirms our request, plane is going to hang off
      SwitchRoutingRequest srr = prm.forAtc().getRoutingForSwitchRequest(this.getAtcId(), callsign);
      if (srr != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (acceptsNewRouting(callsign, srr) == false)
          rejectChangedRouting(callsign, targetAtcId, psm);
        else
          prm.forAtc().confirmRerouting(this.getAtcId(), callsign);
      }
    } else if (prm.forAtc().isUnderSwitchRequest(callsign, null, this.getAtcId())) {
      // other ATC offers us a plane
      RequestResult planeAcceptance = canIAcceptPlane(callsign);
      if (planeAcceptance.isAccepted) {
        acceptSwitch(callsign, targetAtcId, psm);
      } else {
        rejectSwitch(callsign, targetAtcId, planeAcceptance, psm);
      }
    }
  }

  private void rejectChangedRouting(Callsign callsign, AtcId targetAtcId, PlaneSwitchRequest psr) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().resetSwitchRequest(this.getAtcId(), callsign);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new AtcRejection(psr, "New routing rejected."));
    sendMessage(m);
  }

  private void rejectSwitch(Callsign callsign, AtcId targetAtcId, RequestResult planeAcceptance, PlaneSwitchRequest psr) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().rejectSwitchRequest(callsign, this.getAtcId());
    Message nm = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new AtcRejection(psr, planeAcceptance.message));
    sendMessage(nm);
  }

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }


}
