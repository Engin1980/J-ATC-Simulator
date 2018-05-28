/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.extenders;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Marek
 */
public class XmlFileSelectorExtender {

  private final JTextField txt;
  private final JButton btn;
  private File file;

  public JTextField getTextControl(){
    return txt;
  }

  public JButton getButtonControl(){
    return btn;
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, File file) {
    this.txt = txt;
    this.btn = btn;
    this.btn.addActionListener(e -> processDialog());
    this.txt.setEditable(false);
    setFile(file);
    this.btn.setText("(browse)");
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn) {
    this(txt, btn, null);
  }

  public XmlFileSelectorExtender() {
    this(new JTextField(), new JButton("(browse)"));
  }

  public final void setFileName(String fileName) {
    if (fileName == null)
      setFile(null);
    else
      setFile(new File(fileName));
  }

  public final void setFile(File file) {
    this.file = file;
    if (this.file == null)
      this.txt.setText("< browse for file >");
    else
      this.txt.setText(this.file.getPath());
  }

  public final String getFileName(){
    if (file == null)
      return null;
    else
      return file.toString();
  }

  public final File getFile() {
    return file;
  }

  private void processDialog() {
    JFileChooser jfc = new JFileChooser();

    jfc.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));

    int res = jfc.showOpenDialog(txt);

    if (res != JFileChooser.APPROVE_OPTION) {
      return; // cancel etc.
    }
    setFile(jfc.getSelectedFile());
  }

  public boolean isValid() {
    return file != null && file.exists();
  }

}
