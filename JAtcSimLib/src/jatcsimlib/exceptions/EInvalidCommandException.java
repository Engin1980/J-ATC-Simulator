/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.exceptions;

/**
 *
 * @author Marek
 */
public class EInvalidCommandException extends ERuntimeException {

  public EInvalidCommandException(String message, String processedCommandLinePart, String errorCommandLinePart) {
    super(message + " (processed: " + processedCommandLinePart + "; failed command: " + errorCommandLinePart + ")");
  }
  
}
