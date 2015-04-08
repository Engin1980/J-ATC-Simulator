/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.messaging;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
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

  /**
   * Flag if is some new atc message for user added.
   */
  private boolean newAtcMessageForUserAtc = false;
  /**
   * Flag if is some new plane message for user added.
   */
  private boolean newPlaneMessageForUserAtc = false;
  /**
   *
   */
  private boolean newSystemMessageForUserAtc = false;

  private final Map<Object, MessageList> inner = new HashMap<>();

  public void addMessage(Message m) {

    _addMessage(m);

    _flightRecordMessageIf(m);
  }

  private void _addMessage(Message m) {
    if (inner.containsKey(m.target) == false) {
      inner.put(m.target, new MessageList());
    }

    if (m.isFromAtcMessage() && m.target == Acc.atcApp()) {
      if (newAtcMessageForUserAtc == false) {
        newAtcMessageForUserAtc = true;
      }
    } else if (m.isFromPlaneMessage() && m.target == Acc.atcApp()) {
      if (newPlaneMessageForUserAtc == false) {
        newPlaneMessageForUserAtc = true;
      }
    }
    if (m.isFromSystemMessage() && m.target == Acc.atcApp()) {
      if (newSystemMessageForUserAtc == false) {
        newSystemMessageForUserAtc = true;
      }
    }

    inner.get(m.target).add(m);
  }

  public String getTime() {
    return Acc.now().toString();
  }

  /**
   * Deletes messages older than ETime.
   *
   * @param time Minimal ETime of deleted messages.
   */
  public void deleteOldMessages(ETime time) {
    for (Object key : inner.keySet()) {
      inner.get(key).removeOld(time);
    }
  }

  /**
   * Returns all system messages.
   *
   * @param clearRetrieved
   * @return
   */
  public List<Message> getSystems(boolean clearRetrieved) {
    return getMy(Message.SYSTEM, clearRetrieved);
  }

  /**
   * Returns all messages submitted for "target" object.
   *
   * @param target What is target object, which items should be returned?
   * @param clearRetrieved Clear items which were returned?
   * @return Messages of the "target" object.
   */
  public List<Message> getMy(Object target, boolean clearRetrieved) {
    return getMy(target, Acc.now(), clearRetrieved);
  }

  /**
   * Returns all messages submitted for "target" object older than ETime.
   *
   * @param target What is target object, which items should be returned?
   * @param time Only older messages than this time will be returned.
   * @param cleanRetrieved Clear items which were returned?
   * @return Messages of the "target" object older than ETime
   */
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

  /**
   * Removes specific message
   *
   * @param m Message to remove
   */
  public void remove(Message m) {
    inner.get(m.target).remove(m);
  }

  public boolean isNewAtcMessage() {
    return newAtcMessageForUserAtc;
  }

  public boolean isNewPlaneMessage() {
    return newPlaneMessageForUserAtc;
  }

  public boolean isNewSystemMessage() {
    return newSystemMessageForUserAtc;
  }

  public void resetNewMessagesFlag() {
    newAtcMessageForUserAtc = false;
    newPlaneMessageForUserAtc = false;
    newSystemMessageForUserAtc = false;
  }

  private void _flightRecordMessageIf(Message m) {
    if (m.source instanceof Airplane) {
      ((Airplane) m.source).getFlightRecorder().logCVR(m);
    }
    if (m.target instanceof Airplane) {
      ((Airplane) m.target).getFlightRecorder().logCVR(m);
    }
  }
}
