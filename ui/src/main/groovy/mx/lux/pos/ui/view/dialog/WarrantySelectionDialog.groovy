package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.resources.UI_Standards
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateUtils

import javax.swing.*
import java.awt.*
import java.util.List

class WarrantySelectionDialog extends JDialog {

  private def sb = new SwingBuilder()

  private DefaultTableModel warrantyModel
  private JTable tWarrantis

  Item selection
  Boolean canceled
  String armazon
  private List<Item> lstItems = new ArrayList<>() as List<Item>

  WarrantySelectionDialog( List<Item> lstItems, Item itemArm ) {
    this.lstItems.addAll( lstItems )
    armazon = itemArm.name
    canceled = false
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Seleccionar fechas",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 400, 280 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap ", "[grow,fill]", "10[]10[]" ) ) {
          label( text: "Seleccione la garantia para el articulo ${armazon}" )
          scrollPane( border: titledBorder( title: 'Garantias' ) ) {
            tWarrantis = table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
              warrantyModel = tableModel( list: lstItems ) {
                closureColumn( header: 'Garantia', read: {Item tmp -> StringUtils.trimToEmpty(tmp?.name)} )
              } as DefaultTableModel
            }
          }
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Aplicar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonOk() }
            )
            button( text: "No Aplicar", preferredSize: UI_Standards.BUTTON_SIZE,
              actionPerformed: { onButtonClose() }
            )
            button( text: "Cancelar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonCancel() }
            )
          }
        }
      }

    }
  }

  // UI Management
  protected void refreshUI( ) {

  }

  // Public Methods
  void activate( ) {
    refreshUI()
    setVisible( true )
  }

  Item getSelectedWarranty( ) {
    return selection
  }

  Boolean getCanceled( ) {
    return canceled
  }

  // UI Response
  protected void onButtonCancel( ) {
    selection = null
    canceled = true
    dispose()
  }

  protected void onButtonClose( ) {
    selection = null
    dispose()
  }

  protected void onButtonOk( ) {
    int index = tWarrantis.convertRowIndexToModel(tWarrantis.getSelectedRow())
    if (tWarrantis.selectedRowCount > 0) {
      selection = this.lstItems.getAt(index)
      dispose()
    } else {
      sb.doLater {
        dispose()
      }
    }

  }
}
