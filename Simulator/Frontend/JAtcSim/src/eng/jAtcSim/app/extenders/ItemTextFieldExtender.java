package eng.jAtcSim.app.extenders;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ArrayUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ItemTextFieldExtender {

  private static final Color FAIL_COLOR = new Color(255, 150, 150);
  private JTextField txt;
  private String[] model = new String[0];
  private Color okColor = null;

  public ItemTextFieldExtender() {
    this.txt = new JTextField();
    this.txt.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        update();
      }
    });
  }

  public String[] getModel() {
    return model;
  }

  public void setModel(String[] model) {
    this.model = model;
    update();
  }

  public JTextField getControl() {
    return txt;
  }

  public String[] getItems() {
    String[] tmp = txt.getText().split(";");
    IList<String> lst = new EList<>(tmp);
    tmp = lst.select(q->q.trim()).where(q->q.length() > 0).distinct().toArray(String.class);
    return tmp;
  }
  public void setItems(String[] items){
    IList<String> lst = new EList<>(items);
    EStringBuilder t = new EStringBuilder();
    t.appendItems(lst,q->q, ";" );
    txt.setText(t.toString());
  }

  private void update() {
    boolean isOk = true;

    String[] items = getItems();
    for (String item : items) {
      if (ArrayUtils.contains(model, item) == false) {
        isOk = false;
        break;
      }
    }

    if (isOk) {
      txt.setBackground(okColor);
      okColor = null;
    } else {
      if (okColor == null) okColor = txt.getBackground();
      txt.setBackground(FAIL_COLOR);
    }
  }
}
