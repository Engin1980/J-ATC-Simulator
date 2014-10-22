/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.messaging;

import jatcsimlib.Acc;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandList;
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

  private boolean newAtcMessageForUserAtc = false;
  private boolean newPlaneMessageForUserAtc = false;

  private final Map<Object, MessageList> inner = new HashMap<>();

  public void addMessage(Object source, Object target, String text) {
    Message m = new Message(source, target, new StringMessage(text));
    addMessage(m);
  }

  public void addMessage(Object source, Object target, Command content) {
    CommandList cmds = new CommandList();
    cmds.add(content);
    Message m = new Message(source, target, cmds);
    addMessage(m);
  }

  public void addMessage(Object source, Object target, IContent content) {
    Message m = new Message(source, target, content);
    addMessage(m);
  }

  public void addMessage(int delay, Object source, Object target, IContent content) {
    Message m = new Message(Acc.now().addSeconds(delay), source, target, content);
    addMessage(m);
  }

  public void addMessage(int delay, Object source, Object target, String content) {
    addMessage(delay, source, target, new StringMessage(content));
  }

  public void addMessage(Message m) {

    _addMessage(m);
  }

  private void _addMessage(Message m) {
    if (inner.containsKey(m.target) == false) {
      inner.put(m.target, new MessageList());
    }

    if (m.isAtcMessage() && m.target == Acc.atcApp()) {
      if (newAtcMessageForUserAtc == false) {
        newAtcMessageForUserAtc = true;
      }
    } else if (m.isPlaneMessage() && m.target == Acc.atcApp()) {
      if (newPlaneMessageForUserAtc == false) {
        newPlaneMessageForUserAtc = true;
      }
    }

    inner.get(m.target).add(m);
  }

  public String getTime() {
    return Acc.now().toString();
  }

  public void deleteOldMessages(ETime time) {
    for (Object key : inner.keySet()) {
      inner.get(key).removeOld(time);
    }
  }

  public List<Message> getMy(Object target, boolean clearRetrieved) {
    return getMy(target, Acc.now(), clearRetrieved);
  }

  public List<Message> getMy(Object target, ETime time, boolean cleanRetrieved) {
    if (inner.containsKey(target) == false) {
      return new ArrayList<>();
    }

    List<Message> ret = inner.get(target).cloneVisibleToList(time);
    if (cleanRetrieved) {
      inner.get(target).removeAll(ret);
    }

    return ret;
  }

  public void remove(Message m) {
    inner.get(m.target).remove(m);
  }

  public boolean isNewAtcMessage() {
    return newAtcMessageForUserAtc;
  }

  public boolean isNewPlaneMessage() {
    return newPlaneMessageForUserAtc;
  }

  public void resetNewMessagesFlag() {
    newAtcMessageForUserAtc = false;
    newPlaneMessageForUserAtc = false;
  }

}
