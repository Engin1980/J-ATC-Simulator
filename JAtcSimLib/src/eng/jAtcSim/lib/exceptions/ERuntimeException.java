/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.exceptions;

/**
 * @author Marek
 */
public class ERuntimeException extends RuntimeException {

  public ERuntimeException(String message) {
    super(message);
  }

  public ERuntimeException(String text, Object... params) {
    super(String.format(text, params));
  }

  public ERuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public ERuntimeException(Throwable cause, String text, Object... params) {
    super(String.format(text, params), cause);
  }

}
