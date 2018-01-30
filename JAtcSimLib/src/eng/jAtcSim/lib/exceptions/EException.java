/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.exceptions;

/**
 *
 * @author Marek
 */
public class EException extends Exception {

  public EException(String message) {
    super(message);
  }

  public EException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
