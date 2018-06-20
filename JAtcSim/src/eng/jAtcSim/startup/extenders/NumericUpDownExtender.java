/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.extenders;

import eng.eSystem.events.EventSimple;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * @author Marek Vajgl
 */
public class NumericUpDownExtender {
  private final JSpinner nud;
  private final int minimum;
  private final int maximum;
  private EventSimple<NumericUpDownExtender> onChanged = new EventSimple<>(this);

  public NumericUpDownExtender(JSpinner nud, int minimum, int maximum, int value, int step) {
    if (nud == null)
      throw new IllegalArgumentException("Argument \"nud\" cannot be null.");
    assert value >= minimum;
    assert value <= maximum;

    this.nud = nud;

    this.minimum = minimum;
    this.maximum = maximum;
    SpinnerModel model =
        new SpinnerNumberModel(value, minimum, maximum, step);
    this.nud.setModel(model);

    this.nud.addChangeListener(q -> onChanged.raise());
  }

  public JSpinner getControl() {
    return nud;
  }

  public int getMinimum() {
    return this.minimum;
  }

  public int getMaximum() {
    return this.maximum;
  }

  public int getValue() {
    return (int) this.nud.getValue();
  }

  public void setValue(int value) {
    this.nud.setValue(value);
  }

  public EventSimple<NumericUpDownExtender> getOnChanged() {
    return onChanged;
  }
}
