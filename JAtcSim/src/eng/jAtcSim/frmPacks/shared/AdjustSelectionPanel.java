package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.swing.*;
import eng.eSystem.swing.extenders.BoxItem;
import eng.eSystem.swing.extenders.CheckedListBoxExtender;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AdjustSelectionPanel<T> extends JPanel {

  private eng.eSystem.swing.extenders.CheckedListBoxExtender<T> lstBox =
      new CheckedListBoxExtender<>();
  private DialogResult dialogResult = DialogResult.none;

  public AdjustSelectionPanel() {
    initComponents();
  }

  public void resetDialogResult() {
    this.dialogResult = DialogResult.none;
  }

  private void initComponents() {
    JScrollPane pnlLst = new JScrollPane(lstBox.getControl());
    JPanel pnlBtns = LayoutManager.createGridPanel(2, 2, 0,
        Factory.createButton("(all)", this::btnCheckAll_click),
        Factory.createButton("(none)", this::btnCheckNone_click),
        Factory.createButton("Cancel", this::btnCancel_click),
        Factory.createButton("Ok", this::btnOk_click));

    LayoutManager.fillBorderedPanel(this, null, pnlBtns, null, null, pnlLst);
  }

  public DialogResult getDialogResult() {
    return dialogResult;
  }

  private void btnOk_click(ActionEvent actionEvent) {
    EventQueue.invokeLater(() -> {
      dialogResult = DialogResult.ok;
      this.getRootPane().getParent().setVisible(false);
    });
  }

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

  public void setItems(IReadOnlyList<T> items, Selector<T, String> labelSelector) {
    lstBox.setDefaultLabelSelector(labelSelector);
    lstBox.addItems(items);
  }

  public void setItems(Iterable<BoxItem<T>> items){
    items.forEach(q->lstBox.addItem(q));
  }

  public void setCheckedItems(Iterable<T> items) {
    lstBox.setCheckedItems(items);
  }

  public Iterable<T> getCheckedItems() {
    IReadOnlySet<T> ret = lstBox.getCheckedItems();
    return ret;
  }

}
