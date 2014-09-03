/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.messaging;

import jatcsimlib.global.ETime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class MessageList {
  private List<Message> inner = new ArrayList<>();
  private boolean sorted = false;
  // nebude tu lepší "linked-list"?
  
  public void add (Message m){
    inner.add(m);
    sorted = false;
  }

  void removeAll(List<Message> lst) {
    inner.removeAll(lst);
  }

  private void sort() {
    Collections.sort(inner);
    sorted = true;
  }
  
  public void removeOld (ETime time){
     List<Message> toRem = new ArrayList<>();
    for (Message m : inner) {
      if (time.isAfter(m.displayToTime)) {
        toRem.add(m);
      }
    }
    this.inner.removeAll(toRem);
  }

  List<Message> cloneVisibleToList(ETime time) {
    List<Message> ret = new LinkedList<>();
    for(Message m : inner){
      if (time.isBetween(m.displayFromTime, m.displayToTime))
        ret.add(m);
    }
    return ret;
  }

  void remove(Message m) {
    inner.remove(m);
  }
  
  
}
