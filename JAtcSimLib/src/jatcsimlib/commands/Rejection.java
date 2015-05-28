/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

/**
 *
 * @author Marek Vajgl
 */
public class Rejection extends Answer{
  
  public String reason;

  public Rejection(String reason, Command origin) {
    super(origin);
    this.reason = reason;
  }

  /**
   * Reason of the rejection.
   * @return 
   */
  public String getReason() {
    return reason;
  }
  
}
