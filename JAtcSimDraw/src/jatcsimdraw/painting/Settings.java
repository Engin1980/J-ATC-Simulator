/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.painting;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.types.KeyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Settings {
  private final KeyList<DispSett, String> dispSetts = new KeyList();
  
  public DispSett getDispSett(String key){
    
    DispSett ret = dispSetts.get(key);
    if (ret == null)
      throw new ERuntimeException("No disp-sett key \"" + key + "\" found in settings. Xml file invalid?");
    
    return ret;
  }
}
