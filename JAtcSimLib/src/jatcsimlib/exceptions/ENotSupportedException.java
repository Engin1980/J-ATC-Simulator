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
public class ENotSupportedException extends ERuntimeException {

  public ENotSupportedException() {
    super("Not supported operation.");
  }

  public ENotSupportedException(String message) {
    super("Not supported operation. " + message);
  }
  
}
