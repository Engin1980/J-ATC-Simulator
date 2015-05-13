/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.messaging;

/**
 *
 * @author Marek
 */
public class GoingAroundStringMessageContent extends StringMessageContent {

  public GoingAroundStringMessageContent(String reason) {
    super("Going around. " + reason);
  }
}
