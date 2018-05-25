package eng.jAtcSim.startup.extenders;

import eng.eSystem.collections.IMap;

import javax.swing.*;

public class XComboBoxExtender<T> {

  private static class Item<T>{
    public String label;
    public T value;

    public Item(String label, T value) {
      this.label = label;
      this.value = value;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private final JComboBox cmb;
  private final DefaultComboBoxModel model = new DefaultComboBoxModel();


  public XComboBoxExtender(JComboBox cmb, IMap<String, T> items) {
    this.cmb = cmb;

    for (String key : items.getKeys()) {
      T val = items.get(key);
      Item<T> it = new Item<>(key, val);
      model.addElement(it);
    }
    cmb.setModel(model);
  }

  public JComboBox getControl(){
    return cmb;
  }

  public T getSelectedItem(){
    Item<T> ret = (Item<T>) cmb.getSelectedItem();
    return ret.value;
  }

  public int getSelectedIndex(){
    int ret = cmb.getSelectedIndex();
    return ret;
  }
}
