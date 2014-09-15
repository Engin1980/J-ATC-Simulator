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
public class EBindException extends ERuntimeException {

  public EBindException(String message) {
    super(message);
  }

  public EBindException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
