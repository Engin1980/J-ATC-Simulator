package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.planeResponsibility.diagrams.SwitchInfo;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.*;
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
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  private static class PlaneSwitchWrapper {

    public static boolean isPlaneSwitchBased(Message message) {
      PlaneSwitchRequest ps = tryGetBaseIfBasedOnPlaneSwitch(message);
      return ps != null;
    }

    public static PlaneSwitchRequest tryGetBaseIfBasedOnPlaneSwitch(Message message) {
      if (message.getContent() instanceof PlaneSwitchRequest)
        return (PlaneSwitchRequest) message.getContent();
      else if (message.getContent() instanceof AtcConfirmation)
        return (PlaneSwitchRequest) ((AtcConfirmation) message.getContent()).getOrigin();
      else if (message.getContent() instanceof AtcRejection)
        return (PlaneSwitchRequest) ((AtcRejection) message.getContent()).getOrigin();
      else
        return null;
    }

    private final Message message;

    public PlaneSwitchWrapper(Message message) {
      EAssert.isTrue(isPlaneSwitchBased(message));
      this.message = message;
    }

    public PlaneSwitchRequestRouting getRouting() {
      return getPlaneSwitch().getRouting();
    }

    public AtcId getSource() {
      Participant p = message.getSource();
      EAssert.isTrue(p.getType() == Participant.eType.atc);
      AtcId ret = Context.Internal.getAtc(p.getId()).getAtcId();
      return ret;
    }

    public Squawk getSquawk() {
      return getPlaneSwitch().getSquawk();
    }

    public AtcId getTarget() {
      Participant p = message.getTarget();
      EAssert.isTrue(p.getType() == Participant.eType.atc);
      AtcId ret = Context.Internal.getAtc(p.getId()).getAtcId();
      return ret;
    }

    public PlaneSwitchRequest getPlaneSwitch() {
      return tryGetBaseIfBasedOnPlaneSwitch(this.message);
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

  private class SwitchManager {

    private static final int SECONDS_BEFORE_REPEAT_SWITCH_REQUEST = 30;
    private final ComputerAtc parent = ComputerAtc.this;

    public void elapseSecond() {
      this.elapCheckAndProcessPlanesReadyToSwitch();
      this.elapRepeatOldSwitchRequests();
    }

    public void processPlaneSwitchMessage(PlaneSwitchWrapper mw) {
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

        AtcId targetAtcId = parent.getTargetAtcIfPlaneIsReadyToSwitchToAnotherAtc(airplane);
        if (targetAtcId != null) {
          this.elapRequestNewSwitch(airplane, targetAtcId);
        }
      }
    }

    private boolean isPlaneSwitchRelated(Message m, Squawk otherSquawk) {
      PlaneSwitchRequest ps = PlaneSwitchWrapper.tryGetBaseIfBasedOnPlaneSwitch(m);
      if (ps == null) return false;
      else return ps.getSquawk().equals(otherSquawk);
    }

    private void elapRepeatOldSwitchRequests() {
      EDayTimeRun now = Context.getShared().getNow();
      IReadOnlyList<SwitchInfo> awaitings = this.parent.outgoingPlanes.where(
              q -> q.getTime().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

      for (SwitchInfo si : awaitings) {
        IAirplane airplane = si.getAirplane();
        if (speechDelayer.isAny(q -> isPlaneSwitchRelated(q, si.getAirplane().getSqwk())))
          continue; // if message about this plane is delayed and waiting to process
        Squawk sqwk = airplane.getSqwk();
        Message m = new Message(
                Participant.createAtc(parent.getAtcId()),
                Participant.createAtc(si.getAtcId()),
                new PlaneSwitchRequest(sqwk, true));
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
              new PlaneSwitchRequest(airplane.getSqwk(), false));
      sendMessage(m);

      Context.Internal.getPre().openProceedingSwitch(airplane.getSqwk());
    }

    private void msgConfirm(PlaneSwitchWrapper mw) {
      AtcConfirmation confirmation = new AtcConfirmation(mw.getPlaneSwitch());
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(mw.getSource()),
              confirmation
      );
      sendMessage(msg);
    }

    private void msgReject(PlaneSwitchWrapper mw, String reason) {
      AtcRejection confirmation = new AtcRejection(mw.getPlaneSwitch(), reason);
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(mw.getSource()),
              confirmation
      );
      sendMessage(msg);
    }

    private void procAcceptNewSwitchRequest(PlaneSwitchWrapper mw) {
      IAirplane airplane = Context.Internal.getPlane(mw.getSquawk());
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

    private void procProcessConfirmedSwitch(SwitchInfo si, PlaneSwitchWrapper mw) {
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

    private void procProcessIncomingNewSwitchRequest(PlaneSwitchWrapper mw) {
      IAirplane plane = Context.Internal.getPlane(mw.getSquawk());
      RequestResult planeAcceptance = canIAcceptPlaneIncomingFromAnotherAtc(plane);
      if (planeAcceptance.isAccepted) {
        procAcceptNewSwitchRequest(mw);
        Context.Internal.getPre().closeProceedingSwitch(mw.getSquawk());
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

  @Override
  public boolean isResponsibleFor(Callsign callsign) {
    return this.planes.isAny(q -> q.getCallsign().equals(callsign));
  }

  protected abstract boolean acceptsNewRouting(IAirplane airplane, PlaneSwitchRequestRouting routing);

  protected abstract RequestResult canIAcceptPlaneIncomingFromAnotherAtc(IAirplane plane);

  /**
   * Returns target atc if plane is ready for switch.
   *
   * @param airplane Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  protected abstract AtcId getTargetAtcIfPlaneIsReadyToSwitchToAnotherAtc(IAirplane airplane);

  protected abstract void processMessagesFromPlane(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs);

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract boolean shouldBeSwitched(Callsign plane);

  private void confirmGoodDayNotificationIfRequired(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs) {
    IList<GoodDayNotification> gdns = spchs.whereItemClassIs(GoodDayNotification.class, false);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
      lst.add(new RadarContactConfirmationNotification());
      SwitchInfo si = this.incomingPlanes.tryGetFirst(q -> q.getAirplane().getCallsign().equals(plane.getCallsign()));
      if (si == null) {
        AtcId atcId = Context.Internal.getAtc(AtcType.app).getAtcId();
        lst.add(new ContactCommand(atcId));
      } else {
        this.incomingPlanes.remove(si);
      }
      Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAirplane(plane.getCallsign()),
              lst);
      sendMessage(msg);
    }
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    PlaneSwitchWrapper w = tryGetWrapperIfMessageIsRelatedToPlaneSwitching(m);
    if (w != null) {
      this.switchManager.processPlaneSwitchMessage(w);
    } else {
      processNonPlaneSwitchMessageFromAtc(m);
    }
  }

  private PlaneSwitchWrapper tryGetWrapperIfMessageIsRelatedToPlaneSwitching(Message m) {
    if (PlaneSwitchWrapper.isPlaneSwitchBased(m))
      return new PlaneSwitchWrapper(m);
    else
      return null;
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        super.getRecorder().write(m); // incoming item

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

    if (spchs.containsType(GoodDayNotification.class))
      confirmGoodDayNotificationIfRequired(plane, spchs);
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
