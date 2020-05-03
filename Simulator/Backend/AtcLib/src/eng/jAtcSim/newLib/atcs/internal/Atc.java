package eng.jAtcSim.newLib.atcs.internal;

import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public abstract class Atc {

  protected final AtcId atcId;
  protected final int acceptAltitude;
  protected final int releaseAltitude;
  protected final int orderedAltitude;
  protected final AtcRecorder recorder;


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

  public abstract void unregisterPlaneUnderControl(Callsign plane);

  public abstract void removePlaneDeletedFromGame(Callsign plane);

  public abstract void registerNewPlaneUnderControl(Callsign plane, boolean initialRegistration);

  public Atc(eng.jAtcSim.newLib.area.Atc template) {
    this.atcId = new AtcId(
        template.getName(), template.getFrequency(), template.getType());
    this.acceptAltitude = template.getAcceptAltitude();
    this.releaseAltitude = template.getReleaseAltitude();
    this.orderedAltitude = template.getOrderedAltitude();

    this.recorder = AtcRecorder.create(this);
  }

  public abstract void init();

  public abstract boolean isHuman();

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public AtcId getAtcId() {
    return atcId;
  }

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  @Override
  public String toString() {
    return this.atcId.getName();
  }

  protected void sendMessage(Message msg) {
    MessagingAcc.getMessenger().send(msg);
    recorder.write(msg);
  }

}
