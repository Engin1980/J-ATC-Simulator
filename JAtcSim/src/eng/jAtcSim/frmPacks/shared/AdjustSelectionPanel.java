package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.swing.*;
import eng.eSystem.swing.extenders.BoxItem;
import eng.eSystem.swing.extenders.ListBoxExtender;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

public class AdjustSelectionPanel<T> extends JPanel {

  private eng.eSystem.swing.extenders.ListBoxExtender<T> lstBox =
      new ListBoxExtender<>();
  private DialogResult dialogResult = DialogResult.none;

  public AdjustSelectionPanel() {
    initComponents();
  }

  private void initComponents() {
    JScrollPane pnlLst = new JScrollPane(lstBox.getControl());
    JPanel pnlBtns = LayoutManager.createGridPanel(2, 2, 0,
        Factory.createButton("(all)", this::btnSelectAll_click),
        Factory.createButton("(none)", this::btnSelectNone_click),
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

  private void btnSelectNone_click(ActionEvent actionEvent) {
    lstBox.selectNone();
  }

  private void btnSelectAll_click(ActionEvent actionEvent) {
    lstBox.selectAll();
  }

  public void setItems(IReadOnlyList<T> items, Selector<T, String> labelSelector) {
    lstBox.setDefaultLabelSelector(labelSelector);
    lstBox.addItems(items);
  }

  public void setItems(Iterable<BoxItem<T>> items){
    items.forEach(q->lstBox.addItem(q));
  }

  public void setSelectedItems(IReadOnlyList<T> items) {
    lstBox.setSelectedItems(items);
  }

  public IReadOnlyList<T> getSelectedItems() {
    IReadOnlyList<T> ret = lstBox.getSelectedItems();
    return ret;
  }

}
