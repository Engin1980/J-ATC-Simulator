package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class Atc {

  private final AtcType type;

  private final String name;
  private final double frequency;
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

  public abstract void unregisterPlaneUnderControl(IAirplane4Atc plane);

  public abstract void removePlaneDeletedFromGame(IAirplane4Atc plane);

  public abstract void registerNewPlaneUnderControl(IAirplane4Atc plane, boolean initialRegistration);

  protected PlaneResponsibilityManager.PlaneResponsibilityManagerForAtc getPrm(){
    return Acc.prm().forAtc();
  }

  public Atc(eng.jAtcSim.newLib.area.Atc template) {
    this.type = template.getType();
    this.name = template.getName();
    this.frequency = template.getFrequency();
    this.acceptAltitude = template.getAcceptAltitude();
    this.releaseAltitude = template.getReleaseAltitude();
    this.orderedAltitude = template.getOrderedAltitude();

    this.recorder = AtcRecorder.create(this);
  }

  public abstract void init();

  public abstract boolean isHuman();

  public AtcType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getReleaseAltitude() {
    return releaseAltitude;
  }

  public double getFrequency() {
    return frequency;
  }

  public int getAcceptAltitude() {
    return acceptAltitude;
  }

  public int getOrderedAltitude() {
    return orderedAltitude;
  }

  private AtcId atcId = null;
  public AtcId getAtcId(){
    if (atcId == null)
      this.atcId = new AtcId(this.name, this.frequency, this.type);
    return this.atcId;
  }

  @Override
  public String toString() {
    return this.name;
  }

  protected void sendMessage(Message msg){
    LAcc.getMessenger().send(msg);
    recorder.write(msg);
  }

}
