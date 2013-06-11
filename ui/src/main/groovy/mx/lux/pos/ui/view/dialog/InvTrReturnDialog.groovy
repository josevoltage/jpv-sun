package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.ICorporateKeyVerifier
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.model.InvTr
import mx.lux.pos.ui.view.component.NumericTextField
import mx.lux.pos.ui.view.component.PercentTextField
import net.miginfocom.swing.MigLayout

import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*
import java.awt.Point
import org.apache.commons.lang3.StringUtils

class InvTrReturnDialog extends JDialog {

  private static final String TXT_DIALOG_TITLE = "Devolucion ajena a Sucursal"
  private static final String TXT_AMOUNT_LABEL = "Monto"
  private static final String TXT_TICKET_LABEL = "Ticket"
  private static final String TXT_EMPLOYEE_LABEL = "Empleado"
  private static final String TXT_BUTTON_CANCEL = "Cancelar"
  private static final String TXT_BUTTON_OK = "Imprimir"

  private static final Double ZERO_TOLERANCE = 0.001

  private SwingBuilder sb = new SwingBuilder( )

  private Font bigLabel
  private Font bigInput

  private JTextField txtTicket
  private JTextField txtEmployee
  private NumericTextField txtAmount
  private JButton btnOk
  private InvTr data

  private Boolean cancelPressed

  InvTrReturnDialog(  ) {
    init( )
    buildUI( )
    setupTriggers( )
  }
  
  // Internal Methods
  protected void assign() {
    this.data.ticketNum = StringUtils.trimToEmpty( this.txtTicket.text )
    this.data.empName = StringUtils.trimToEmpty( this.txtEmployee.text )
    this.data.returnAmount = this.txtAmount.value
  }

  protected void display() {
    this.txtTicket.text = StringUtils.trimToEmpty( this.data.ticketNum )
    this.txtEmployee.text = StringUtils.trimToEmpty( this.data.empName )
    this.txtAmount.value = this.data.returnAmount
  }

  protected void init( ) {
    bigLabel = new Font( '', Font.PLAIN, 14 )
    bigInput = new Font( '', Font.BOLD, 14 )
    txtAmount = new NumericTextField( )
  }
  
  protected void buildUI( ) {
    sb.dialog( this,
        title: ( TXT_DIALOG_TITLE ) ,
        location: [ 300, 300 ] as Point,
        resizable: false,
        modal: true,
        pack: true,
    ) {
      borderLayout( )
      panel( constraints: BorderLayout.CENTER,
          layout: new MigLayout( "wrap 2", "[]30[fill,grow,200!]", "[]10[]10[]"),
          border: BorderFactory.createEmptyBorder( 10, 20, 0, 20)
      ) {
        label( text: TXT_TICKET_LABEL, font: bigLabel )
        txtTicket = textField(
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,
            actionPerformed: {  }
        )
        label( TXT_EMPLOYEE_LABEL, font: bigLabel )
        txtEmployee = textField(
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,
            actionPerformed: {  }
        )
        label( TXT_AMOUNT_LABEL, font: bigLabel )
        textField( txtAmount,
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,
            actionPerformed: {  }
        )
      }
      
      panel( constraints: BorderLayout.PAGE_END,
          border: BorderFactory.createEmptyBorder( 0, 10, 10, 20 )
      ) {
        borderLayout( )
        panel( constraints: BorderLayout.LINE_END ) {
          button( text: TXT_BUTTON_CANCEL,
              preferredSize: UI_Standards.BUTTON_SIZE,
              actionPerformed: { onButtonCancel( ) }
          )
          btnOk = button( text: TXT_BUTTON_OK,
              preferredSize: UI_Standards.BUTTON_SIZE,
              actionPerformed: { onButtonOk( ) } 
          )
        }
      }
    }
  }

  protected void setupTriggers() {

  }

  // Public methods
  void activate( ) {
    this.cancelPressed = false
    this.display()
    this.setVisible( true )
  }

  Boolean isCancel() {
    this.assign()
    return this.cancelPressed
  }

  void setData( InvTr pData ) {
    this.data = pData
  }

  // UI Response
  void onButtonCancel() {
    this.assign()
    this.cancelPressed = true
    setVisible( false )  
  }
  
  void onButtonOk() {
    this.assign( )
    this.data.postReturn()
    setVisible( false )
  }
  
}
