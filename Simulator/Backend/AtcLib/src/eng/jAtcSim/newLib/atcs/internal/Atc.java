package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public abstract class Atc implements IXPersistable {

  private AtcId atcId = null;
  private int acceptAltitude = 0;
  private int releaseAltitude = 0;
  private int orderedAltitude = 0;
  @XIgnored
  private AtcRecorder recorder = null;

  @XmlConstructor
  protected Atc() {
    PostContracts.register(this, () -> atcId != null);
    PostContracts.register(this, () -> this.recorder != null);
  }

  public Atc(eng.jAtcSim.newLib.area.Atc template) {
    this();
    this.atcId = template.toAtcId();
    this.acceptAltitude = template.getAcceptAltitude();
    this.releaseAltitude = template.getReleaseAltitude();
    this.orderedAltitude = template.getOrderedAltitude();
  }

  public abstract void elapseSecond();

  public abstract boolean isResponsibleFor(Callsign callsign);

  public abstract void unregisterPlaneDeletedFromGame(Callsign plane, boolean isForcedDeletion);

  public abstract void registerNewPlaneInGame(Callsign plane, boolean initialRegistration);

  public abstract boolean isHuman();

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public AtcId getAtcId() {
    return atcId;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public void init() {
    this.recorder = AtcRecorder.create(this.getAtcId());
  }

  @Override
  public String toString() {
    return this.atcId.getName();
  }

  protected IList<Message> pullMessages() {
    IList<Message> messages = Context.getMessaging().getMessenger().getMessagesByListener(
            Participant.createAtc(this.getAtcId()), true);

    messages.forEach(q -> this.recorder.write(q));

    return messages;
  }

  protected void sendMessage(Message msg) {
    Context.getMessaging().getMessenger().send(msg);
    recorder.write(msg);
  }
}
