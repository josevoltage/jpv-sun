package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.DetalleNotaVenta
import mx.lux.pos.model.NotaVenta
import mx.lux.pos.ui.model.Payment
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout
import java.util.List

import javax.swing.*
import java.awt.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class CorrectedOrdersDialog extends JDialog {

  private DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
  private DateVerifier dv = DateVerifier.instance
  private def sb = new SwingBuilder()

  private DefaultTableModel orderModel

  private List<DetalleNotaVenta> lstNotasDet = new ArrayList<>()
  public boolean button = false

    CorrectedOrdersDialog( List<NotaVenta> lstNotasVenta, List<NotaVenta> lstNotasCan ) {
    for(NotaVenta nv : lstNotasVenta){
      nv.udf5 = "V"
      lstNotasDet.addAll( nv.detalles )
    }

    for(NotaVenta nv : lstNotasCan){
      lstNotasDet.addAll( nv.detalles )
    }
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Notas Corregidas",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 500, 320 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap ", "[grow,fill]", "20[]10[]" ) ) {
            label( text: "Notas a las que se agrego la transaccion" )
            scrollPane( border: titledBorder( title: 'Notas' ) ) {
                table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
                    orderModel = tableModel( list: lstNotasDet ) {
                        closureColumn( header: 'Tipo', read: {DetalleNotaVenta tmp -> tmp?.notaVenta?.udf5?.contains("V") ? "VENTA" : "DEVOLUCION"} )
                        closureColumn( header: 'Factura', read: {DetalleNotaVenta tmp -> tmp?.notaVenta?.factura} )
                        closureColumn( header: 'Fecha Venta', read: {DetalleNotaVenta tmp -> tmp?.notaVenta?.fechaHoraFactura?.format("dd-MM-yyyy")} )
                        closureColumn( header: 'Articulo', read: {DetalleNotaVenta tmp -> tmp?.articulo?.articulo} )
                        closureColumn( header: 'Cantidad', read: {DetalleNotaVenta tmp -> tmp?.cantidadFac?.intValue()} )
                    } as DefaultTableModel
                }
            }
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Aceptar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonOk() }
            )
          }
        }
      }

    }
  }

  // UI Management

  // Public Methods

  // UI Response

  protected void onButtonOk( ) {
    dispose()
  }
}
