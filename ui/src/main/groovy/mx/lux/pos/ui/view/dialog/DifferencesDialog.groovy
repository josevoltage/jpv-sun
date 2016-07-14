package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.Diferencia
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.controller.ItemController
import mx.lux.pos.ui.model.Differences
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer

import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.*
import java.awt.event.MouseEvent
import java.util.List

class DifferencesDialog extends JDialog {

  private SwingBuilder sb
  private String code
  private List<Diferencia> suggestions = new ArrayList<Diferencia>()
  private DefaultTableModel model
  private JTable tableItems
  private static final Integer COLUMN_ID = 0

    DifferencesDialog( Component parent ) {
    this.suggestions.addAll( ItemController.findAllDifferences() )
    sb = new SwingBuilder()
    buildUI( )
  }

  private void buildUI(  ) {
    sb.dialog( this,
        title: "Diferencias",
        location: [ 200, 250 ],
        resizable: false,
        preferredSize: [ 570  , 380 ] as Dimension,
        modal: true,
        pack: true,
    ) {
      panel(border: BorderFactory.createEmptyBorder(5, 8, 5, 8)) {
      borderLayout()
      panel(constraints: BorderLayout.PAGE_START, border: BorderFactory.createEmptyBorder(0,0,3,0)) {
        borderLayout()
        label( " ",
               constraints: BorderLayout.PAGE_START)
        label( text: "Total de diferencias: ${suggestions.size()}", minimumSize: [10, 3] as Dimension)
      }
      scrollPane( constraints: BorderLayout.CENTER ) {
        tableItems = table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
          model = tableModel( list: suggestions ) {
            closureColumn( header: 'Sku', read: {Diferencia tmp -> tmp?.id}, maxWidth: 110 )
            closureColumn( header: 'Articulo', read: {Diferencia tmp -> tmp?.articulo.articulo}, maxWidth: 200 )
            closureColumn( header: 'Cant. Fisico', read: {Diferencia tmp -> tmp?.cantidadFisico}, maxWidth: 115 )
            closureColumn( header: 'Cant. Soi', read: {Diferencia tmp -> tmp?.cantidadSoi}, maxWidth: 115 )
            closureColumn( header: 'Diferencias', read: {Diferencia tmp -> tmp?.diferencias}, maxWidth: 115 )
          } as DefaultTableModel
        }
      }
      panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
              button( text: "Imprimir", preferredSize: UI_Standards.BUTTON_SIZE,
                      actionPerformed: { onButtonOk() }
              )
              button( text: "Archivo", preferredSize: UI_Standards.BUTTON_SIZE,
                      actionPerformed: { onGenerateFile() }
              )
              button( text: "Cerrar", preferredSize: UI_Standards.BUTTON_SIZE,
                      actionPerformed: { onButtonCancel() }
              )
          }
      }
    }
    }
  }


  protected void onButtonCancel( ) {
    dispose()
  }


  protected void onButtonOk( ) {
    ItemController.printDifferences()
  }

  protected void onGenerateFile( ) {
    Boolean generated = ItemController.generateDifferencesFile()
    if( generated ){
        JOptionPane.showMessageDialog( new JDialog(), "El archivo se genero en la carpeta ${Registry.diferencesPath}",
                "Archivo de diferencias", JOptionPane.INFORMATION_MESSAGE )
    } else {
        JOptionPane.showMessageDialog( new JDialog(), "El archivo de diferencias no se genero correctamente",
                "Archivo de diferencias", JOptionPane.INFORMATION_MESSAGE )
    }
  }

}
