/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.shared;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class EventManager<Tparent, Klistener extends EventListener, CeventArg> {
  private final Tparent parent;
  private final List<Klistener> lst = new ArrayList();

  public EventManager(Tparent parent) {
    this.parent = parent;
  }

  public void addListener(Klistener listener) {
    lst.add(listener);
  }

  public void removeListener(Klistener listener) {
    if (lst.contains(listener)) {
      lst.remove(listener);
    }
  }
  
  public void raise(CeventArg e){
    for (Klistener l : lst){
      l.raise(parent, e);
    }
  }
}
