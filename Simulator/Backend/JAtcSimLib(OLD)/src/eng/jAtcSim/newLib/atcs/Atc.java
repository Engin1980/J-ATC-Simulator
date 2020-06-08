package eng.jAtcSim.newLib.area.atcs;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplane4Atc;
import eng.jAtcSim.newLib.area.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.area.serialization.LoadSave;

/**
 *
 * @author Marek
 */
public abstract class Atc implements IMessageParticipant {
  
  public enum eType{
    gnd,
    twr,
    app,
    ctr
  }
  
  private final eType type;
  
  private final String name;
  private final double frequency;
  protected final int acceptAltitude;
  protected final int releaseAltitude;
  protected final int orderedAltitude;

  public final void save(XElement elm){
    XElement tmp =new XElement("atc");
    elm.addElement(tmp);

    LoadSave.saveField(tmp, this, "name");
    _save(tmp);
  }

  public void load(XElement elm) {
      XElement tmp = null;
      for (XElement item : elm.getChildren()) {
        if (item.getChild("name").getContent().equals(name)){
          tmp = item;
          break;
        }
      }
      assert tmp != null;

      _load(tmp);
  }

  protected abstract void _save(XElement elm);
  protected abstract void _load(XElement elm);

  @XmlIgnore
  protected final AtcRecorder recorder;

  public abstract void unregisterPlaneUnderControl(IAirplane4Atc plane);

  public abstract void removePlaneDeletedFromGame(IAirplane4Atc plane);

  public abstract void registerNewPlaneUnderControl(IAirplane4Atc plane, boolean initialRegistration);

  protected PlaneResponsibilityManager.PlaneResponsibilityManagerForAtc getPrm(){
    return Acc.prm().forAtc();
  }

  public Atc(AtcTemplate template) {
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

  public eType getType() {
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

  @Override
  public String toString() {
    return this.name;
  }

  protected void sendMessage(Message msg){
    Acc.messenger().send(msg);
    recorder.write(msg);
  }

}
