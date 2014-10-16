/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.messaging;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.PlaneSwitchMessage;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandList;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;

/**
 *
 * @author Marek
 */
public class Message implements Comparable<Message> {

  protected static int planeVisibleTimeInSeconds = 30;
  protected static int atcVisibleTimeInSeconds = 30;
  protected static int systemVisibleTimeInSeconds = 30;

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
  public final IContent content;

  public static Message create (Object source, Object target, Command c){
    CommandList cmdLst = new CommandList();
    cmdLst.add(c);
    Message ret = new Message(source, target, cmdLst);
    return ret;
  }
  
  public Message(Object source, Object target, IContent content) {
    this(Acc.now(), source, target, content);
  }

  public Message(ETime creationTime, Object source, Object target, IContent content) {
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
      throw new ENotSupportedException("Type of source: " + source.getClass().getSimpleName());
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
  
  public CommandList getAsCommands(){
    return (CommandList) content;
  }
  public PlaneSwitchMessage getAsPlaneSwitchMessage(){
    return (PlaneSwitchMessage) content;
  }
  public StringMessage getAsString(){
    return (StringMessage) content;
  }

  public String toContentString() {
    if (content instanceof CommandList)
      return "--?? set of commands ??--";
    else if (content instanceof PlaneSwitchMessage){
      return getAsPlaneSwitchMessage().getAsString();
    } else if (content instanceof StringMessage){
      return getAsString().text;
    } else
      throw new ENotSupportedException();
  }
}
