package eng.jAtcSim.newLib.atcs.internal;

import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public abstract class Atc {

  private final AtcId atcId;
  private final int acceptAltitude;
  private final int releaseAltitude;
  private final int orderedAltitude;
  private final AtcRecorder recorder;

  public Atc(eng.jAtcSim.newLib.area.Atc template) {
    this.atcId = template.toAtcId();
    this.acceptAltitude = template.getAcceptAltitude();
    this.releaseAltitude = template.getReleaseAltitude();
    this.orderedAltitude = template.getOrderedAltitude();

    this.recorder = AtcRecorder.create(this.getAtcId());
  }

  // region abstract
  public abstract boolean isResponsibleFor(Callsign callsign);

  public abstract void unregisterPlaneDeletedFromGame(Callsign plane, boolean isForcedDeletion);

  public abstract void registerNewPlaneInGame(Callsign plane, boolean initialRegistration);

  public abstract void init();

  public abstract boolean isHuman();
// endregion abstract

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

  @Override
  public String toString() {
    return this.atcId.getName();
  }

  protected void sendMessage(Message msg) {
    Context.getMessaging().getMessenger().send(msg);
    recorder.write(msg);
  }

  protected AtcRecorder getRecorder() {
    return recorder;
  }

//  public final void save(XElement elm){
//    XElement tmp =new XElement("atc");
//    elm.addElement(tmp);
//
//    LoadSave.saveField(tmp, this, "name");
//    _save(tmp);
//  }
//
//  public void load(XElement elm) {
//    XElement tmp = null;
//    for (XElement item : elm.getChildren()) {
//      if (item.getChild("name").getContent().equals(name)){
//        tmp = item;
//        break;
//      }
//    }
//    assert tmp != null;
//
//    _load(tmp);
//  }
//
//  protected abstract void _save(XElement elm);
//  protected abstract void _load(XElement elm);
}
