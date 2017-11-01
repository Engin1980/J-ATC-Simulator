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
import jatcsimlib.commands.formatting.Formatters;
import jatcsimlib.commands.formatting.LongFormatter;
import jatcsimlib.commands.formatting.ShortFormatter;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;

/**
 *
 * @author Marek
 */
public class Message implements Comparable<Message> {
  
  public final static Object SYSTEM = new Object();

  protected static int planeVisibleTimeInSeconds = 30;
  protected static int atcVisibleTimeInSeconds = 30;
  protected static int systemVisibleTimeInSeconds = 30;

  protected static int minPlaneDelayInSeconds = 0; //3;
  protected static int maxPlaneDelayInSeconds = 0; //10;
  protected static int minAtcDelayInSeconds = 0; // 3;
  protected static int maxAtcDelayInSeconds = 0; //10;
  protected static int minUserAtcDelayInSeconds = 1;
  protected static int maxUserAtcDelayInSeconds = 10;

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
    Message ret = new Message(null, source, target, cmdLst);
    return ret;
  }
  
  public static Message create (Object source, Object target, Command c, int secondsDelay){
    CommandList cmdLst = new CommandList();
    cmdLst.add(c);
    Message ret = new Message(secondsDelay, source, target, cmdLst);
    return ret;
  }
  
  public static Message create(Airplane source, Atc target, IContent content){
    Message ret = new Message(source, target, content);
    return ret;
  }
  
  public static Message create(Airplane source, Atc target, IContent content, int secondsDelay){
    Message ret = new Message(source, target, content);
    return ret;
  }
  
  public static Message create(Airplane source, Atc target, String text, int secondsDelay){
    StringMessageContent sm = new StringMessageContent(text);
    Message ret = new Message(secondsDelay, source, target, sm);
    return ret;
  }
  
  public static Message create(Airplane source, Atc target, String text){
    return create(source, target, new StringMessageContent(text));
  }
  
  public static Message create(Atc source, Atc target, IContent content){
    Message ret = new Message(source, target, content);
    return ret;
  }
  
  public static Message create(Atc source, Atc target, String text){
    return create(source, target, new StringMessageContent(text));
  }
  
  public static Message create(Atc source, Airplane plane, IContent content){
    Message ret = new Message(source, plane, content);
    return ret;
  }
  
  public static Message createForSystem(UserAtc source, String text){
    StringMessageContent sm = new StringMessageContent(text);
    Message ret = new Message(source, SYSTEM, sm);
    return ret;
  }
    
  public static Message createFromSystem(UserAtc target, String text){
    StringMessageContent sm = new StringMessageContent(text);
    Message ret = new Message(SYSTEM, target, sm);
    return ret;
  }
  
  private Message(int secondsDelay, Object source, Object target, IContent content){
    this (Acc.now().addSeconds(secondsDelay), source, target, content);
  }
  
  private Message(Object source, Object target, IContent content){
    this(Acc.now(), source, target, content);
  }
  
  private Message(ETime creationTime, Object source, Object target, IContent content) {
    if (content == null) {
      throw new IllegalArgumentException("Argument \"content\" cannot be null.");
    }
    if (target == null) {
      throw new IllegalArgumentException("Argument \"target\" cannot be null.");
    }
    if (creationTime == null)
      creationTime = Acc.now();

    this.creationTime = creationTime.clone();
    this.displayFromTime = this.creationTime.addSeconds(generateDelay(source));
    this.displayToTime = this.displayFromTime.addSeconds(generateVisible(source));
    this.source = source;
    this.target = target;
    this.content = content;
  }

  public boolean isFromPlaneMessage() {
    return source != null && source instanceof Airplane;
  }

  public boolean isFromAtcMessage() {
    return source != null && source instanceof Atc;
  }

  public boolean isFromSystemMessage() {
    return source == SYSTEM;
  }

  private static int generateDelay(Object source) {
    if (source == SYSTEM) {
      return rnd.nextInt(minSystemDelayInSeconds, maxSystemDelayInSeconds);
    } else if (source instanceof Airplane) {
      return rnd.nextInt(minPlaneDelayInSeconds, maxPlaneDelayInSeconds);
    } else if (source instanceof UserAtc) {
      return rnd.nextInt(minUserAtcDelayInSeconds, maxUserAtcDelayInSeconds);
    } else if (source instanceof Atc) {
      return rnd.nextInt(minAtcDelayInSeconds, maxAtcDelayInSeconds);
    } else {
      throw new ENotSupportedException("Type of source: " + source.getClass().getSimpleName());
    }
  }

  private static int generateVisible(Object source) {
    if (source == SYSTEM) {
      return systemVisibleTimeInSeconds;
    } else if (source instanceof Airplane) {
      return planeVisibleTimeInSeconds;
    } else if (source instanceof UserAtc) {
      return 10; // ai_ATC must be able to check the speech
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
    if (source == SYSTEM)
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
  public StringMessageContent getAsString(){
    return (StringMessageContent) content;
  }

  public String toContentString() {
    if (content instanceof CommandList)
      return Formatters.format((CommandList) content, LongFormatter.getInstance());
    else if (content instanceof PlaneSwitchMessage){
      return getAsPlaneSwitchMessage().getAsString();
    } else if (content instanceof StringMessageContent){
      return getAsString().text;
    } else
      throw new ENotSupportedException();
  }
}
