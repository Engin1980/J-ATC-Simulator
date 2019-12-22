/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.exceptions;

/**
 *
 * @author Marek
 */
public class EInvalidCommandException extends RuntimeException {

  public EInvalidCommandException(String message, String processedCommandLinePart, String errorCommandLinePart) {
    super(message + " (processed: " + processedCommandLinePart + "; failed command: " + errorCommandLinePart + ")");
  }
   public EInvalidCommandException(String message, String errorCommandLinePart) {
    super(message + " (failed command: " + errorCommandLinePart + ")");
  }
  
}
