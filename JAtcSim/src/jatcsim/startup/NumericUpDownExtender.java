/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Marek Vajgl
 */
public class NumericUpDownExtender {
  private final JSpinner nud;

  public NumericUpDownExtender(JSpinner nud, int minimum, int maximum, int value, int step) {
    if (nud == null)
      throw new IllegalArgumentException("Argument \"nud\" cannot be null.");

    this.nud = nud;
    
    SpinnerModel model =
        new SpinnerNumberModel(value, minimum, maximum, step);
    this.nud.setModel(model);
  }
  
  public int getValue(){
    return (int) this.nud.getValue();
  }
  
  public void setValue(int value){
    this.nud.setValue(value);
  }
  
}
