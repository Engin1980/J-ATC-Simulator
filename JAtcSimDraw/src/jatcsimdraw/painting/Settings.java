/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.painting;

import jatcsimlib.atcs.Atc;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyList;

/**
 *
 * @author Marek
 */
public class Settings {
  private final KeyList<DispItem, String> dispItems = new KeyList();
  private final KeyList<DispPlane, Atc.eType> dispPlanes = new KeyList();
  
  public DispItem getDispItem(String key){
    
    DispItem ret = dispItems.get(key);
    if (ret == null)
      throw new ERuntimeException("No disp-item key \"" + key + "\" found in settings. Xml file invalid?");
    
    return ret;
  }

  public DispPlane getDispPlane(Atc.eType atcType) {
    return dispPlanes.get(atcType);
  }
  
}
