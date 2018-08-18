package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.*;
import eng.eSystem.swing.*;
import eng.eSystem.swing.extenders.BoxItem;
import eng.eSystem.swing.extenders.CheckedListBoxExtender;
import eng.eSystem.utilites.Selector;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AdjustSelectionPanel<T> extends JPanel {

  private eng.eSystem.swing.extenders.CheckedListBoxExtender<T> lstBox =
      new CheckedListBoxExtender<>();
  private DialogResult dialogResult = DialogResult.none;
  private ISet<T> checkedItems = new ESet<>();
  private ISet<T> modelItems = new ESet<>();

  public AdjustSelectionPanel() {
    initComponents();
  }

  public void resetDialogResult() {
    this.dialogResult = DialogResult.none;
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

  private void txtFilter_keyPressed(JTextField txt, KeyEvent e){
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
      if (checkedItems.contains(visibleItem)){
        if (!this.checkedItems.contains(visibleItem)) this.checkedItems.add(visibleItems);
      } else {
        if (this.checkedItems.contains(visibleItem)) this.checkedItems.remove(visibleItems);
      }
    }
  }

  public DialogResult getDialogResult() {
    return dialogResult;
  }

  private void btnOk_click(ActionEvent actionEvent) {
    syncCheckedItems();
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
    this.modelItems.add(items);
    lstBox.setDefaultLabelSelector(labelSelector);
    lstBox.addItems(items);
  }

  public void setItems(Iterable<BoxItem<T>> items) {
    items.forEach(q->this.modelItems.add(q.value));
    items.forEach(q -> lstBox.addItem(q));
  }

  public void setCheckedItems(Iterable<T> items) {
    this.checkedItems.clear();
    this.checkedItems.add(items);
    lstBox.setCheckedItems(items);
  }

  public Iterable<T> getCheckedItems() {
    IReadOnlySet<T> ret = this.checkedItems;
    return ret;
  }

}
