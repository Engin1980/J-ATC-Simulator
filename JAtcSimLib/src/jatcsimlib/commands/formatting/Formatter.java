/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.commands.*;

/**
 *
 * @author Marek Vajgl
 */
public interface Formatter {
  
  public String format(AfterAltitudeCommand cmd);
  public String format(AfterNavaidCommand cmd);
  public String format(AfterSpeedCommand cmd);
  public String format(ChangeAltitudeCommand cmd);
  public String format(ChangeHeadingCommand cmd);
  public String format(ChangeSpeedCommand cmd);
  public String format(ClearedForTakeoffCommand cmd);
  public String format(ClearedToApproachCommand cmd);
  public String format(ContactCommand cmd);
  public String format(HoldCommand cmd);
  public String format(ProceedDirectCommand cmd);
  public String format(ShortcutCommand cmd);
  public String format(ThenCommand cmd);
  public String format(ToNavaidCommand cmd);
  
  public String format(GoodDayCommand cmd);
  
  public String format(Confirmation cmd);
  public String format(Rejection cmd);
}
