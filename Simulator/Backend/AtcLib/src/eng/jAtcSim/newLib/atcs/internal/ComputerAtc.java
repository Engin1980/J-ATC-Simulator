package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchInfo;
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

  private static class PlaneSwitchRequestMessageWrapper {
    private final Message message;

    public PlaneSwitchRequestMessageWrapper(Message message) {
      this.message = message;
    }

    public PlaneSwitchRequest getPlaneSwitchRequest() {
      return message.getContent();
    }

    public PlaneSwitchRequest.Routing getRouting() {
      return getPlaneSwitchRequest().getRouting();
    }

    public AtcId getSource() {
      Participant p = message.getSource();
      EAssert.isTrue(message.getSource().getType() == Participant.eType.atc);
      AtcId ret = InternalAcc.getAtc(message.getSource().getId()).getAtcId();
      return ret;
    }

    public Squawk getSquawk() {
      return getPlaneSwitchRequest().getSquawk();
    }

    public AtcId getTarget() {
      Participant p = message.getSource();
      EAssert.isTrue(message.getTarget().getType() == Participant.eType.atc);
      AtcId ret = InternalAcc.getAtc(message.getTarget().getId()).getAtcId();
      return ret;
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

    tohle porad vytahnout do nejake tridy bokem
  private class SwitchManager {

    private static final int SECONDS_BEFORE_REPEAT_SWITCH_REQUEST = 30;
    private final ComputerAtc parent = ComputerAtc.this;

    public void elapseSecond() {
      this.elapCheckAndProcessPlanesReadyToSwitch();
      this.elapRepeatOldSwitchRequests();
    }

    public void processPlaneSwitchMessage(Message m) {
      PlaneSwitchRequestMessageWrapper mw = new PlaneSwitchRequestMessageWrapper(m);

      SwitchInfo outgoingSwitchInfo = parent.outgoingPlanes.tryGetFirst(q ->
          q.getAirplane().getSqwk().equals(mw.getSquawk()) && q.getAtcId().equals(mw.getSource()));
      if (outgoingSwitchInfo != null)
        this.procProcessConfirmedSwitch(outgoingSwitchInfo, mw);
      else
        this.procProcessIncomingNewSwitchRequest(mw);

    }

    private void elapCheckAndProcessPlanesReadyToSwitch() {
      for (IAirplane airplane : parent.planes) {
        if (parent.outgoingPlanes.isAny(q -> q.getAirplane().equals(airplane)))
          continue;

        AtcId targetAtcId = parent.getTargetAtcIfPlaneIsReadyToSwitch(airplane);
        if (targetAtcId != null) {
          this.elapRequestNewSwitch(airplane, targetAtcId);
        }
      }
    }

    private void elapRepeatOldSwitchRequests() {
      EDayTimeRun now = Context.getShared().getNow();
      IReadOnlyList<SwitchInfo> awaitings = this.parent.outgoingPlanes.where(
          q -> q.getTime().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

      for (SwitchInfo si : awaitings) {
        IAirplane airplane = si.getAirplane();
        if (speechDelayer
            .isAny(q -> q.getContent() instanceof PlaneSwitchRequest &&
                ((PlaneSwitchRequest) q.getContent()).getSquawk().equals(airplane.getSqwk())))
          continue; // if message about this plane is delayed and waiting to process
        Squawk sqwk = airplane.getSqwk();
        Message m = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(si.getAtcId()),
            PlaneSwitchRequest.createRequest(sqwk, true));
        Context.getMessaging().getMessenger().send(m);
        si.updateTime(now.toStamp());
        parent.getRecorder().write(m);
      }
    }

    private void elapRequestNewSwitch(IAirplane airplane, AtcId targetAtcId) {
      SwitchInfo si = new SwitchInfo(airplane, targetAtcId, Context.getShared().getNow().toStamp());
      parent.outgoingPlanes.add(si);
      Message m = new Message(
          Participant.createAtc(parent.getAtcId()),
          Participant.createAtc(targetAtcId),
          PlaneSwitchRequest.createRequest(airplane.getSqwk(), false));
      sendMessage(m);
    }

    private void msgConfirm(PlaneSwitchRequestMessageWrapper mw) {
AtcConfirmation confirmation = new AtcConfirmation(mw.getPlaneSwitchRequest());
Message msg = new Message(
    Participant.createAtc(parent.getAtcId()),
    Participant.createAtc(mw.getSource()),
    confirmation
);
      sendMessage(msg);
    }

    private void msgCreate(PlaneSwitchRequest psr,)

    private void msgReject(PlaneSwitchRequestMessageWrapper mw, String reason) {
      AtcRejection confirmation = new AtcRejection(mw.getPlaneSwitchRequest(), reason);
      Message msg = new Message(
          Participant.createAtc(parent.getAtcId()),
          Participant.createAtc(mw.getSource()),
          confirmation
      );
      sendMessage(msg);
    }

    private void procAcceptNewSwitchRequest(PlaneSwitchRequestMessageWrapper mw) {
      IAirplane airplane = InternalAcc.getPlane(mw.getSquawk());
      SwitchInfo si = new SwitchInfo(airplane, mw.getSource(), Context.getShared().getNow().toStamp());
      parent.outgoingPlanes.add(si);
      msgConfirm(mw);
    }

    private void procApplyConfirmedSwitch(SwitchInfo si) {
      AtcId newTargetAtc = si.getAtcId();
      Message msg = new Message(
          Participant.createAtc(parent.getAtcId()),
          Participant.createAirplane(si.getAirplane().getCallsign()),
          new SpeechList<>(
              new ContactCommand(newTargetAtc)));
      Context.getMessaging().getMessenger().send(msg);
    }

    private void procProcessConfirmedSwitch(SwitchInfo si, PlaneSwitchRequestMessageWrapper mw) {
      if (mw.getRouting() != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (!parent.acceptsNewRouting(si.getAirplane(), mw.getRouting()))
          this.msgReject(mw, "Updated routing not accepted.");
        else {
          this.msgConfirm(mw);
          this.procApplyConfirmedSwitch(si);
        }
      } else
        this.procApplyConfirmedSwitch(si);
    }

    private void procProcessIncomingNewSwitchRequest(PlaneSwitchRequestMessageWrapper mw) {
      // other ATC offers us a plane
      RequestResult planeAcceptance = canIAcceptPlane(mw.getSquawk());
      if (planeAcceptance.isAccepted) {
        procAcceptNewSwitchRequest(mw);
      } else {
        msgReject(mw, planeAcceptance.message);
      }
    }
  }

  protected final IList<SwitchInfo> incomingPlanes = new EList<>();
  protected final IList<SwitchInfo> outgoingPlanes = new EList<>();
  protected final IList<IAirplane> planes = new EList<>();
  private final DelayedList<Message> speechDelayer = new DelayedList<>(
      Global.MINIMUM_ATC_SPEECH_DELAY_SECONDS, Global.MAXIMUM_ATC_SPEECH_DELAY_SECONDS);
  private final SwitchManager switchManager = new SwitchManager();

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

  protected abstract boolean acceptsNewRouting(IAirplane airplane, PlaneSwitchRequest.Routing routing);

  protected abstract RequestResult canIAcceptPlane(Squawk squawk);

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
      this.switchManager.processPlaneSwitchMessage(m);
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

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }


}
