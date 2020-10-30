package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.IAtcSwitchManagerInterface;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.xml.ParticipantSerializer;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.Global;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.serializers.ItemsSerializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  private final DelayedList<Message> speechDelayer = new DelayedList<>(
          Global.MINIMUM_ATC_SPEECH_DELAY_SECONDS, Global.MAXIMUM_ATC_SPEECH_DELAY_SECONDS);
  protected SwitchManager switchManager;

  public ComputerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  protected abstract IAtcSwitchManagerInterface getSwitchManagerInterface();

  protected abstract void processMessagesFromPlaneExceptGoodDayNotification(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs);

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract void _elapseSecond();

  protected abstract void __save(XElement target);

  @Override
  public final void elapseSecond() {
    IList<Message> thisSecondMsgs = super.pullMessages();
    speechDelayer.add(thisSecondMsgs);

    IList<Message> msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    this.switchManager.elapseSecond();

    _elapseSecond();
  }

  @Override
  public void init() {
    Context.getMessaging().getMessenger().registerListener(
            Participant.createAtc(this.getAtcId()));
    this.switchManager = new SwitchManager(this.getSwitchManagerInterface(),
            () -> speechDelayer.getAll());
  }

  @Override
  public boolean isHuman() {
    return false;
  }

  @Override
  public boolean isResponsibleFor(Callsign callsign) {
    return this.switchManager.isResponsibleFor(callsign);
  }

  @Override
  protected final void _save(XElement target) {

    IMap<Class<?>, Serializer<?>> customDelayListSerializers = EMap.of(
            Participant.class, new ParticipantSerializer(),
            SpeechList.class, new ItemsSerializer<>(ObjectSerializer.createDeepSerializer())
    );

    XmlSaveUtils.Field.storeField(target, this, "speechDelayer",
            (XElement e, DelayedList<Message> q) -> q.save(e, customDelayListSerializers));
    XmlSaveUtils.Field.storeField(target, this, "switchManager",
            (XElement e, SwitchManager q) -> q.save(e));

    this.__save(target);
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

    if (m.getContent() instanceof PlaneSwitchRequest) {
      this.switchManager.processPlaneSwitchMessage(m.getContent(), sender);
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
            .where(q -> q.isRepeated() == false);
    EAssert.isTrue(goodDayNotifications.isAll(q -> q.getCallsign().equals(plane.getCallsign())));
    goodDayNotifications.forEach(q -> this.switchManager.processGoodDayFromPlane(q));

    processMessagesFromPlaneExceptGoodDayNotification(plane, spchs);
  }

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }
}
