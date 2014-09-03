/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.settings;

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
  private final KeyList<DispText, DispText.eType> dispTexts = new KeyList();

  public DispItem getDispItem(String key) {

    DispItem ret = dispItems.tryGet(key);
    if (ret == null) {
      throw new ERuntimeException("No disp-item key \"" + key + "\" found in settings. Xml file invalid?");
    }

    return ret;
  }

  public DispPlane getDispPlane(Atc.eType atcType) {
    DispPlane ret = dispPlanes.tryGet(atcType);
    if (ret == null) {
      throw new ERuntimeException("No disp-plane key \"" + atcType + "\" found in settings. Xml file invalid?");
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

}
