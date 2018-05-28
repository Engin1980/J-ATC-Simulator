package eng.jAtcSim.startup.extenders;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.events.EventSimple;

import javax.swing.*;

public class XComboBoxExtender<T> {

  public static class Item<T>{
    public final String label;
    public final T value;

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
  private DefaultComboBoxModel model;

  private final EventSimple<XComboBoxExtender> selectedItemChanged = new EventSimple(this);

  public XComboBoxExtender() {
    this(new JComboBox(), new EList<>());
  }

  public EventSimple<XComboBoxExtender> getSelectedItemChanged() {
    return selectedItemChanged;
  }

  public XComboBoxExtender(JComboBox cmb, IList<Item<T>> items) {
    this.cmb = cmb;
    this.cmb.addActionListener(q -> selectedItemChanged.raise());
    setModel(items);
  }

  public JComboBox getControl(){
    return cmb;
  }

  public T getSelectedItem(){
    Item<T> ret = (Item<T>) cmb.getSelectedItem();
    if (ret == null)
      return null;
    else
      return ret.value;
  }

  public final int getSelectedIndex(){
    int ret = cmb.getSelectedIndex();
    return ret;
  }

  public final void setSelectedIndex(int index){
    if (cmb.getItemCount() > index)
      cmb.setSelectedIndex(index);
  }

  public final void setModel(IList<Item<T>> items) {
    model = new DefaultComboBoxModel();
    for (Item<T> item : items) {
      model.addElement(item);
    }
    cmb.setModel(model);
  }

  public void setSelectedItem(T item){
    DefaultComboBoxModel mdl = (DefaultComboBoxModel) cmb.getModel();
    int index = -1;
    for (int i = 0; i < mdl.getSize(); i++) {
      Item<T> lcl = (Item<T>) mdl.getElementAt(i);
      if (lcl.value.equals(item)){
        index = i;
        break;
      }
    }
    if (index == -1){
      mdl.removeAllElements();
      mdl.addElement(new Item<>(item.toString(), item));
      index = mdl.getSize()-1;
    }
    cmb.setSelectedIndex(index);
  }
}
