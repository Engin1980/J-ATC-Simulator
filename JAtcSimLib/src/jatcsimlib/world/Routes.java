/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Routes {
  public static List<Route> getByFilter(Iterable<Route> list, boolean arrival, char category) {
    List<Route> ret = new LinkedList<>();
    
    for (Route r : list){
      if (arrival){
        if (r.getType() == Route.eType.sid) continue;
      } else {
        if (r.getType() != Route.eType.sid) continue;
      }
      
      if (r.isValidForCategory(category) == false) continue;
      
      ret.add(r);
    }
    
    return ret;
  }  
}
