package eng.jAtcSim.startup.extenders;

import eng.eSystem.collections.IList;

import javax.swing.*;

public class ComboBoxExtender<T> {

  private final JComboBox cmb;
  private final DefaultComboBoxModel model = new DefaultComboBoxModel();


  public ComboBoxExtender(JComboBox cmb, IList<T> items) {
    this.cmb = cmb;

    for (T item : items) {
      model.addElement(item);
    }
    cmb.setModel(model);
  }

  public JComboBox getControl(){
    return cmb;
  }

  public T getSelectedItem(){
    T ret = (T) cmb.getSelectedItem();
    return ret;
  }

  public int getSelectedIndex(){
    int ret = cmb.getSelectedIndex();
    return ret;
  }

}
