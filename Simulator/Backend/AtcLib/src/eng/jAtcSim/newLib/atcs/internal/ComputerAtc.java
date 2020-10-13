package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
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

  private static class SwitchInfo {
    private final Squawk squawk;
    private final AtcId atcId;
    private final EDayTimeStamp firstRequest;
    private EDayTimeStamp lastRequest;

    public SwitchInfo(Squawk squawk, AtcId otherAtcId) {
      EAssert.Argument.isNotNull(squawk, "squawk");
      EAssert.Argument.isNotNull(otherAtcId, "otherAtcId");
      this.squawk = squawk;
      this.atcId = otherAtcId;
      this.firstRequest = Context.getShared().getNow().toStamp();
      this.lastRequest = this.firstRequest;
    }

    public AtcId getAtcId() {
      return atcId;
    }

    public EDayTimeStamp getFirstRequest() {
      return firstRequest;
    }

    public EDayTimeStamp getLastRequest() {
      return lastRequest;
    }

    public void setLastRequest(EDayTimeStamp lastRequest) {
      EAssert.Argument.isNotNull(lastRequest, "lastRequest");
      EAssert.Argument.isTrue(lastRequest.isAfter(this.lastRequest));
      this.lastRequest = lastRequest;
    }

    public Squawk getSqwk() {
      return squawk;
    }
  }

  private static class PlaneSwitchWrapper {

    private final Message message;

    public PlaneSwitchWrapper(Message message) {
      EAssert.isTrue(isPlaneSwitchBased(message));
      this.message = message;
    }

    public PlaneSwitchRequest getPlaneSwitch() {
      return tryGetBaseIfBasedOnPlaneSwitch(this.message);
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
              q.getSqwk().equals(mw.getSquawk()) && q.getAtcId().equals(mw.getSource()));
      if (outgoingSwitchInfo != null)
        this.processOutgoingPlaneSwitchMessage(outgoingSwitchInfo, mw);
      else
        this.processIncomingPlaneSwitchMessage(mw);
    }

    private void elapCheckAndProcessPlanesReadyToSwitch() {
      for (IAirplane airplane : parent.planes) {
        if (parent.outgoingPlanes.isAny(q -> q.getSqwk().equals(airplane.getSqwk())))
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
              q -> q.getLastRequest().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

      for (SwitchInfo si : awaitings) {
        if (speechDelayer.isAny(q -> isPlaneSwitchRelated(q, si.getSqwk())))
          continue; // if message about this plane is delayed and waiting to process
        Message m = new Message(
                Participant.createAtc(parent.getAtcId()),
                Participant.createAtc(si.getAtcId()),
                new PlaneSwitchRequest(si.getSqwk(), true));
        parent.sendMessage(m);
        Context.getMessaging().getMessenger().send(m);
        si.setLastRequest(now.toStamp());
      }
    }

    private void elapRequestNewSwitch(IAirplane airplane, AtcId targetAtcId) {
      SwitchInfo si = new SwitchInfo(airplane.getSqwk(), targetAtcId);
      parent.outgoingPlanes.add(si);
      Message m = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(targetAtcId),
              new PlaneSwitchRequest(airplane.getSqwk(), false));
      sendMessage(m);
    }

    private void sendConfirmMessage(PlaneSwitchWrapper mw) {
      AtcConfirmation confirmation = new AtcConfirmation(mw.getPlaneSwitch());
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(mw.getSource()),
              confirmation
      );
      sendMessage(msg);
    }

    private void sendRejectMessage(PlaneSwitchWrapper mw, String reason) {
      AtcRejection confirmation = new AtcRejection(mw.getPlaneSwitch(), reason);
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(mw.getSource()),
              confirmation
      );
      sendMessage(msg);
    }

    private void sendContactCommantToOutgoingPlane(SwitchInfo si) {
      AtcId newTargetAtc = si.getAtcId();
      IAirplane plane = Context.Internal.getPlane(si.getSqwk());
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAirplane(plane.getCallsign()),
              new SpeechList<>(
                      new ContactCommand(newTargetAtc)));
      Context.getMessaging().getMessenger().send(msg);
    }

    private void processOutgoingPlaneSwitchMessage(SwitchInfo si, PlaneSwitchWrapper mw) {
      if (mw.getRouting() != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        IAirplane plane = Context.Internal.getPlane(si.getSqwk());
        if (!parent.acceptsNewRouting(plane, mw.getRouting())) {
          this.sendRejectMessage(mw, sf("Updated routing %s not accepted.", mw.getRouting().toString()));
        } else {
          this.sendContactCommantToOutgoingPlane(si);
        }
      } else
        this.sendContactCommantToOutgoingPlane(si);
      parent.outgoingPlanes.remove(si);
    }

    private void processIncomingPlaneSwitchMessage(PlaneSwitchWrapper mw) {
      IAirplane plane = Context.Internal.getPlane(mw.getSquawk());
      RequestResult planeAcceptance = canIAcceptPlaneIncomingFromAnotherAtc(plane);
      if (planeAcceptance.isAccepted) {
        parent.incomingPlanes.add(plane.getSqwk());
        sendConfirmMessage(mw);
      } else {
        sendRejectMessage(mw, planeAcceptance.message);
      }
    }
  }

  protected final IList<Squawk> incomingPlanes = new EList<>();
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
      if (this.incomingPlanes.isAny(q -> q.equals(plane.getSqwk()))) {
        this.incomingPlanes.remove(plane.getSqwk());
      } else {
        AtcId atcId = Context.Internal.getAtc(AtcType.app).getAtcId();
        lst.add(new ContactCommand(atcId));
      }
      Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAirplane(plane.getCallsign()),
              lst);
      super.sendMessage(msg);
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
    //TODO this should be in Atc class as the
    // logging into recorder should be done there and
    // recorder is not exposed to derived classes
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
