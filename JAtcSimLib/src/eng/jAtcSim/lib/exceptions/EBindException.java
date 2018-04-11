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
public class EBindException extends RuntimeException {

  public EBindException(String message) {
    super(message);
  }

  public EBindException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
