/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.messaging;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;

/**
 *
 * @author Marek
 */
public class Message implements Comparable<Message> {

  protected static int planeVisibleTimeInSeconds = 10;
  protected static int atcVisibleTimeInSeconds = 10;
  protected static int systemVisibleTimeInSeconds = 10;

  protected static int minPlaneDelayInSeconds = 3;
  protected static int maxPlaneDelayInSeconds = 10;
  protected static int minAtcDelayInSeconds = 3;
  protected static int maxAtcDelayInSeconds = 10;

  protected static int minSystemDelayInSeconds = 0;
  protected static int maxSystemDelayInSeconds = 0;

  private static final ERandom rnd = new ERandom();

  protected final ETime creationTime;
  protected final ETime displayFromTime;
  protected final ETime displayToTime;
  public final Object source;
  public final Object target;
  public final Object content;

  public Message(Object source, Object target, Object content) {
    this(Acc.now(), source, target, content);
  }

  public Message(ETime creationTime, Object source, Object target, Object content) {
    if (content == null) {
      throw new IllegalArgumentException("Argument \"content\" cannot be null.");
    }
    if (target == null) {
      throw new IllegalArgumentException("Argument \"target\" cannot be null.");
    }

    this.creationTime = creationTime.clone();
    this.displayFromTime = this.creationTime.addSeconds(generateDelay(source));
    this.displayToTime = this.displayFromTime.addSeconds(generateVisible(source));
    this.source = source;
    this.target = target;
    this.content = content;
  }

  public boolean isPlaneMessage() {
    return source != null && source instanceof Airplane;
  }

  public boolean isAtcMessage() {
    return source != null && source instanceof Atc;
  }

  public boolean isSystemMessage() {
    return source == null;
  }

  private int generateDelay(Object source) {
    if (source == null) {
      return rnd.nextInt(minSystemDelayInSeconds, maxSystemDelayInSeconds);
    } else if (source instanceof Airplane) {
      return rnd.nextInt(minPlaneDelayInSeconds, maxPlaneDelayInSeconds);
    } else if (source instanceof UserAtc) {
      return 0;
    } else if (source instanceof Atc) {
      return rnd.nextInt(minAtcDelayInSeconds, maxAtcDelayInSeconds);
    } else {
      throw new ENotSupportedException();
    }
  }

  private int generateVisible(Object source) {
    if (source == null) {
      return systemVisibleTimeInSeconds;
    } else if (source instanceof Airplane) {
      return planeVisibleTimeInSeconds;
    } else if (source instanceof UserAtc) {
      return 10; // ai_ATC must be able to check the message
    } else if (source instanceof Atc) {
      return atcVisibleTimeInSeconds;
    } else {
      throw new ENotSupportedException();
    }
  }

  @Override
  public int compareTo(Message o) {
    return this.displayFromTime.compareTo(o.displayFromTime);
  }

  public String getText() {
    return (String) content;
  }

  public String tryGetText() {
    String ret;
    try {
      ret = getText();
    } catch (Exception ex) {
      ret = null;
    }
    return ret;
  }

  public String getSourceID() {
    if (source == null)
      return "<system>";
    else if (source instanceof Atc)
      return ((Atc) source).getName();
    else if (source instanceof Airplane)
      return ((Airplane) source).getCallsign().toString();
    else
      throw new ENotSupportedException();
  }

  @Override
  public String toString() {
    return "MSG: " + source + " => " + target + " :: " + content;
  }
  
  
}
