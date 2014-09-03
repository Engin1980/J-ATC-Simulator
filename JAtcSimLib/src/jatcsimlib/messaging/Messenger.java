/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.messaging;

import jatcsimlib.Acc;
import jatcsimlib.Simulation;
import jatcsimlib.atcs.CentreAtc;
import jatcsimlib.global.ETime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marek
 */
public class Messenger {

  private final Map<Object, MessageList> inner = new HashMap<>();

  public void addMessage(Object source, Object target, Object content) {
    Message m = new Message(source, target, content);
    addMessage(m);
  }

  public void addMessage(Message m) {

    if (inner.containsKey(m.target) == false) {
      inner.put(m.target, new MessageList());
    }

    inner.get(m.target).add(m);
  }

  public String getTime() {
    return Simulation.getCurrent().getNow().toString();
  }

  /*
   public List<Message> getVisiblePlaneMessages(ETime time) {
   List<Message> ret = getVisibleMessages(planeMsgs, time);
   return ret;
   }

   public List<Message> getVisibleAtcMessages(ETime time) {
   List<Message> ret = getVisibleMessages(atcMsgs, time);
   return ret;
   }

   public List<Message> getVisibleSystemMessages(ETime time) {
   List<Message> ret = getVisibleMessages(systemMsgs, time);
   return ret;
   }
   */
  public void deleteOldMessages(ETime time) {
    for (Object key : inner.keySet()) {
      inner.get(key).removeOld(time);
    }
  }

  /*
   private List<Message> getVisibleMessages(MessageList msgs, ETime time) {
   List<Message> ret = new ArrayList<>();
   for (Message m : msgs) {
   if (time.isBetween(m.displayFromTime, m.displayToTime)) {
   ret.add(m);
   }
   }
   return ret;
   }
   */
  public List<Message> getMy(Object target) {
    return getMy(target, Acc.now());
  }

  public List<Message> getMy(Object target, ETime time) {
    if (inner.containsKey(target) == false) {
      return new ArrayList<>();
    }

    List<Message> ret = inner.get(target).cloneVisibleToList(time);

    return ret;
  }

  public void remove(Message m) {
    inner.get(m).remove(m);
  }
}