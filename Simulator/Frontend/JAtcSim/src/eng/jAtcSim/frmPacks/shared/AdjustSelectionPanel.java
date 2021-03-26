package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.collections.ISet;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.swing.DialogResult;
import eng.eSystem.swing.Factory;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.CheckedListBoxExtender;
import eng.eSystem.utilites.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AdjustSelectionPanel<T> extends JPanel {

  private final eng.eSystem.swing.extenders.CheckedListBoxExtender<T> lstBox;
  private DialogResult dialogResult = DialogResult.none;
  private final ISet<T> checkedItems = new ESet<>();
  private final ISet<T> modelItems = new ESet<>();

  public AdjustSelectionPanel(Selector<T, String> selector) {
    this.lstBox = new CheckedListBoxExtender<>(new JList(), selector);
    initComponents();
  }

  public Iterable<T> getCheckedItems() {
    IReadOnlySet<T> ret = this.checkedItems;
    return ret;
  }

  public void setCheckedItems(Iterable<T> items) {
    this.checkedItems.clear();
    this.checkedItems.add(items);
    lstBox.setCheckedItems(items);
  }

  public DialogResult getDialogResult() {
    return dialogResult;
  }

  public void resetDialogResult() {
    this.dialogResult = DialogResult.none;
  }

  public void setItems(Iterable<T> items) {
    lstBox.clearItems();
    lstBox.addItems(items);
    items.forEach(q -> this.modelItems.add(q));
  }

  private void initComponents() {
    JScrollPane pnlLst = new JScrollPane(lstBox.getControl());
    JTextField txt = new JTextField();
    txt.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        txtFilter_keyPressed(txt, e);
      }
    });
    JPanel pnlBtns = LayoutManager.createGridPanel(2, 2, 0,
            Factory.createButton("(all)", this::btnCheckAll_click),
            Factory.createButton("(none)", this::btnCheckNone_click),
            Factory.createButton("Cancel", this::btnCancel_click),
            Factory.createButton("Ok", this::btnOk_click));
    JPanel pnlFilter = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 0, txt, pnlBtns);

    LayoutManager.fillBorderedPanel(this, null, pnlFilter, null, null, pnlLst);
  }

  private void txtFilter_keyPressed(JTextField txt, KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
      txt.setText("");

    String s = txt.getText();
    filterByText(s);
  }

  private void filterByText(String s) {
    syncCheckedItems();
    if (StringUtils.isNullOrEmpty(s))
      lstBox.clearFilter();
    else
      lstBox.setFilter(s);
  }

  private void syncCheckedItems() {
    IReadOnlySet<T> visibleItems = lstBox.getItems();
    IReadOnlySet<T> checkedItems = lstBox.getCheckedItems();

    for (T visibleItem : visibleItems) {
      if (checkedItems.contains(visibleItem)) {
        if (!this.checkedItems.contains(visibleItem))
          this.checkedItems.add(visibleItem);
      } else {
        if (this.checkedItems.contains(visibleItem))
          this.checkedItems.remove(visibleItem);
      }
    }
  }

  private void btnOk_click(ActionEvent actionEvent) {
    syncCheckedItems();
    EventQueue.invokeLater(() -> {
      dialogResult = DialogResult.ok;
      this.getRootPane().getParent().setVisible(false);
    });
  }

//  public void setItems(IReadOnlyList<T> items, Selector<T, String> labelSelector) {
//    this.modelItems.add(items);
//    lstBox.setDefaultLabelSelector(labelSelector);
//    lstBox.addItems(items);
//  }

  private void btnCancel_click(ActionEvent actionEvent) {
    dialogResult = DialogResult.cancel;
    this.getRootPane().getParent().setVisible(false);
  }

  private void btnCheckNone_click(ActionEvent actionEvent) {
    lstBox.checkNone();
  }

  private void btnCheckAll_click(ActionEvent actionEvent) {
    lstBox.checkAll();
  }

}
