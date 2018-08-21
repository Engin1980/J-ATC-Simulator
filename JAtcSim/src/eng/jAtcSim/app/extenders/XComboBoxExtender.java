package eng.jAtcSim.app.extenders;

import eng.eSystem.collections.*;
import eng.eSystem.events.EventSimple;

import javax.swing.*;
import java.util.Map;

public class XComboBoxExtender<T> {

  public static class Item<T> {
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

  private final JComboBox<T> cmb;
  private final EventSimple<XComboBoxExtender> selectedItemChanged = new EventSimple(this);

  private static <T> ComboBoxModel<T> convertToModel(IMap<String, T> map) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    for (Map.Entry<String, T> entry : map) {
      Item<T> item = new Item<>(entry.getKey(), entry.getValue());
      model.addElement(item);
    }

    return model;
  }

  private static <T> ComboBoxModel<T> convertToModel(String[] items) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    for (String item : items) {
      model.addElement(new Item(item, item));
    }

    return model;
  }

  private static <T> ComboBoxModel<T> convertToModel(IList<Item<T>> lst) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    for (Item<T> item : lst) {
      model.addElement(item);
    }

    return model;
  }

  public XComboBoxExtender() {
    this(new EList<>());
  }

  public XComboBoxExtender(String[] items) {
    this(
        new JComboBox<>(),
        convertToModel(items)
    );
  }

  public XComboBoxExtender(IList<Item<T>> items) {
    this(
        new JComboBox<>(),
        convertToModel(items)
    );
  }

  public XComboBoxExtender(IMap<String, T> items) {
    this(
        new JComboBox<>(),
        convertToModel(items)
    );
  }

  private XComboBoxExtender(JComboBox<T> cmb, ComboBoxModel<T> model) {
    this.cmb = cmb;
    this.cmb.addActionListener(q -> selectedItemChanged.raise());
    this.setModel(model);
  }

  public JComboBox getControl() {
    return cmb;
  }

  public EventSimple<XComboBoxExtender> getSelectedItemChanged() {
    return selectedItemChanged;
  }

  public T getSelectedItem() {
    Item<T> ret = (Item<T>) cmb.getSelectedItem();
    if (ret == null)
      return null;
    else
      return ret.value;
  }

  public void setSelectedItem(T item) {
    if (item == null) {
      cmb.setSelectedIndex(-1);
    } else {
      DefaultComboBoxModel mdl = (DefaultComboBoxModel) cmb.getModel();
      int index = -1;
      for (int i = 0; i < mdl.getSize(); i++) {
        Item<T> lcl = (Item<T>) mdl.getElementAt(i);
        if (lcl.value.equals(item)) {
          index = i;
          break;
        }
      }
      if (index == -1) {
        mdl.removeAllElements();
        mdl.addElement(new Item<>(item.toString(), item));
        index = mdl.getSize() - 1;
      }
      cmb.setSelectedIndex(index);
    }
  }

  public final int getSelectedIndex() {
    int ret = cmb.getSelectedIndex();
    return ret;
  }

  public final void setSelectedIndex(int index) {
    if (cmb.getItemCount() > index)
      cmb.setSelectedIndex(index);
  }

  public final void setModel(String[] items) {
    ComboBoxModel model = convertToModel(items);
    cmb.setModel(model);
  }

  public final void setModel(IList<Item<T>> items) {
    ComboBoxModel model = convertToModel(items);
    cmb.setModel(model);
  }

  public final void setModel(IMap<String, T> map) {
    ComboBoxModel model = convertToModel(map);
    cmb.setModel(model);
  }

  public final void setModel(ComboBoxModel<T> model) {
    this.cmb.setModel(model);
  }

  public int getCount() {
    return cmb.getModel().getSize();
  }

  public T getItem(int i) {
    Item<T> item = (Item<T>) cmb.getModel().getElementAt(i);
    T ret = item.value;
    return ret;
  }
}
