package mx.lux.pos.ui.view.dialog.dailyclose

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.DailyCloseController
import mx.lux.pos.ui.model.Branch
import mx.lux.pos.ui.model.Payment
import mx.lux.pos.ui.model.Session
import mx.lux.pos.ui.model.SessionItem
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent

class CardResumeDialog extends JDialog {

  private SwingBuilder sb
  private Date date
  //private JTextField invoiceFilter
  private JTextField terminalFilter
  private JTextField planFilter
  private List<Payment> payments
  private DefaultTableModel paymentsModel
  private Branch branch

    CardResumeDialog( Date date ) {
    this.date = date
    sb = new SwingBuilder()
    branch = Session.get( SessionItem.BRANCH ) as Branch
    payments = [ ] as ObservableList
    buildUI()
    fetchPayments()
  }

  private void buildUI( ) {
    sb.dialog( this,
        title: 'Resumen de Tarjetas',
        resizable: false,
        pack: true,
        modal: true,
        layout: new MigLayout( 'fill,wrap', '[fill]' )
    ) {
      panel( border: titledBorder( 'B\u00fasqueda' ), layout: new MigLayout( '', '[][fill,100][][fill,100]' ) ) {
        /*label( 'Ticket  ' )
        label( "${branch?.costCenter}-" )
        invoiceFilter = textField(  )*/
        label( 'Ticket' )
        terminalFilter = textField()
        button( 'Buscar', actionPerformed: doSearch )
        button( 'Limpiar', actionPerformed: doClean )
      }

      scrollPane( border: titledBorder( 'Pagos con Tarjeta' ), constraints: 'h 250!' ) {
        table( selectionMode: ListSelectionModel.SINGLE_SELECTION ) {
          paymentsModel = tableModel( list: payments ) {
            closureColumn( header: 'Ticket', read: {Payment tmp -> tmp?.factura} )
            closureColumn( header: 'Plan', read: {Payment tmp -> tmp?.planId} )
            closureColumn( header: 'Importe', read: {Payment tmp -> tmp?.amount} )
          } as DefaultTableModel
        }
      }

      panel( layout: new MigLayout( 'wrap 3', '[fill,grow][right][right]' ) ) {
        label( " " )
        button( text: 'Imprimir', actionPerformed: doPrint )
        button( text: 'Cerrar', actionPerformed: {dispose()} )
      }
    }
  }

  private void fetchPayments( ) {
    payments.clear()
    payments.addAll( DailyCloseController.findTpvPaymentsByDayByInvoice( date, terminalFilter.text ) )
    paymentsModel.fireTableDataChanged()
  }

  private def doSearch = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    payments.clear()
    payments.addAll( DailyCloseController.findTpvPaymentsByDayByInvoice( date, terminalFilter.text ) )
    paymentsModel.fireTableDataChanged()
    source.enabled = true
  }

  private def doClean = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    //invoiceFilter.text = null
    terminalFilter.text = null
    //planFilter.text = null
    fetchPayments()
    source.enabled = true
  }

  private def doPrint = { ActionEvent ev ->
    DailyCloseController.printCardResume( date )
  }


}
