/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.atcs;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.serialization.LoadSave;

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

  public abstract void unregisterPlaneUnderControl(Airplane plane);

  public abstract void removePlaneDeletedFromGame(Airplane plane);

  public abstract void registerNewPlaneUnderControl(Airplane plane, boolean initialRegistration);

  protected PlaneResponsibilityManager getPrm(){
    return Acc.prm();
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
  
  public final boolean isControllingAirplane(Airplane plane){
    return getPrm().getResponsibleAtc(plane) == this;
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
