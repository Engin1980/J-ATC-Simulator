/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

import jatcsimlib.exceptions.ERuntimeException;

/**
 *
 * @author Marek
 */
public abstract class MustBeBinded {
  private boolean binded;
  
  public void checkBinded(){
    if (!binded){
      throw new ERuntimeException(
          "Object " + this.getClass().getSimpleName() + 
              " has not been binded. Did you call \"bind()\" method over area after loading?");
    }
  }
  
  public void bind(){
    _bind();
    binded = true;
  }
  protected abstract void _bind();

  public boolean isBinded() {
    return binded;
  }
}
