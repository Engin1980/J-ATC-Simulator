/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar.settings;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyList;

/**
 *
 * @author Marek
 */
public class Settings {

  private final KeyList<DispItem, String> dispItems = new KeyList();
  private final KeyList<DispPlane, DispPlane.eBehavior> dispPlanes = new KeyList();
  private final KeyList<DispText, DispText.eType> dispTexts = new KeyList();
  private int refreshRate;

  public DispItem getDispItem(String key) {

    DispItem ret = dispItems.tryGet(key);
    if (ret == null) {
      throw new ERuntimeException("No disp-item key \"" + key + "\" found in settings. Xml file invalid?");
    }

    return ret;
  }

  public DispPlane getDispPlane(Airplane.AirplaneInfo planeInfo) {
    DispPlane ret = null;
    for (DispPlane dp : dispPlanes){
      if (isMatch(dp, planeInfo)){
        ret = dp;
      }
      
      if (ret != null) break;
    }
    
    return ret;
  }

  public DispText getDispText(DispText.eType type) {
    DispText ret = dispTexts.tryGet(type);
    if (ret == null) {
      throw new ERuntimeException("No disp-text key \"" + type + "\" found in settings. Xml file invalid?");
    }
    return ret;
  }

  private boolean isMatch(DispPlane dp, Airplane.AirplaneInfo planeInfo) {
    switch (dp.getBehavior()){
      case stopped:
        return planeInfo.speed() == 0;
      case app:
        return planeInfo.responsibleAtc().getType() == Atc.eType.app;
      case twr:
        return planeInfo.responsibleAtc().getType() == Atc.eType.twr;
      case ctr:
        return planeInfo.responsibleAtc().getType() == Atc.eType.ctr;
      default:
        throw new ENotSupportedException();
    }
  }

  public int getRefreshRate() {
    return refreshRate;
  }

}
