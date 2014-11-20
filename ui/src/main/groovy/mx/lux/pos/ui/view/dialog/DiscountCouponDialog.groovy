package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.model.CuponMv
import mx.lux.pos.model.DescuentoClave
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.model.ICorporateKeyVerifier
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.component.NumericTextField
import mx.lux.pos.ui.view.component.PercentTextField
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils

import javax.swing.*
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class DiscountCouponDialog extends JDialog {



  private static final String TXT_AMOUNT_LABEL = "Monto"
  private static final String TXT_CORPORATE_KEY_LABEL = "Clave Descuento"
  private static final String TXT_PERCENT_LABEL = "% Descuento"
  private static final String TXT_BUTTON_CANCEL = "Cancelar"
  private static final String TXT_BUTTON_OK = "Aplicar"
  private static final String TXT_WARNING_MAX_AMOUNT = "Límite de descuento en tienda: %.1f%% (%,.2f)"
  private static final String TXT_VERIFY_PASS = ""
  private static final String TXT_VERIFY_FAILED = "Clave incorrecta"
  private static  JLabel porceLabel = new JLabel()
  private static  JTextField porceText = new JTextField()

  private static final Double ZERO_TOLERANCE = 0.001

  private SwingBuilder sb = new SwingBuilder( )

  private Font bigLabel
  private Font bigInput

  private NumericTextField txtDiscountAmount
  private PercentTextField txtDiscountPercent
  private JTextField txtCorporateKey
  private JButton btnOk
  private JLabel lblStatus

  private FocusListener trgDiscAmountLeave
  private FocusListener trgDiscPercentLeave
  private FocusListener trgCorporateKeyLeave

  DescuentoClave descuentoClave
  ICorporateKeyVerifier verifier
  Double orderTotal = 0
  Double maximumDiscount = 0
  Boolean corporateEnabled = false
  BigDecimal discountAmt = 0
  Double discountPct = 0
  Boolean discountSelected
  String idOrder

    DiscountCouponDialog( Boolean pCorporate, String idOrder ) {
    corporateEnabled = pCorporate
    this.idOrder = idOrder
    init( )
    buildUI( )
    setupTriggers( )
  }

  // Internal Methods
  protected void init( ) {
    bigLabel = new Font( '', Font.PLAIN, 14 )
    bigInput = new Font( '', Font.BOLD, 14 )
    txtDiscountAmount = new NumericTextField( )
    txtDiscountPercent = new PercentTextField( )
  }
  
  protected void buildUI( JComponent pParent) {
    sb.dialog( this,
        title: "Descuento por Cupon" ,
        location: [ 300, 300 ] ,
        resizable: false,
        modal: true,
        pack: true,
    ) {
      borderLayout( )
      panel( constraints: BorderLayout.CENTER,
          layout: new MigLayout( "wrap 2", "[]30[fill,grow,140!]", "[]10[]10[]"),
          border: BorderFactory.createEmptyBorder( 10, 20, 0, 20)
      ) {
          if ( corporateEnabled ) {
              label( TXT_CORPORATE_KEY_LABEL, font: bigLabel )
              txtCorporateKey = textField( font: bigInput,
                      horizontalAlignment: JTextField.LEFT,
                      actionPerformed: { onCorporateKeyLeave( ) }
              )
          }

       porceLabel =   label( text: TXT_PERCENT_LABEL, font: bigLabel,visible: false )
       porceText = textField( txtDiscountPercent,
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,
            actionPerformed: { onDiscountPercentLeave( ) }  ,visible: false
        )
        label( TXT_AMOUNT_LABEL, font: bigLabel )
        textField( txtDiscountAmount, 
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,

            actionPerformed: { onDiscountAmountLeave( ) } 
        )

        lblStatus = label( TXT_WARNING_MAX_AMOUNT, 
            foreground: UI_Standards.WARNING_FOREGROUND,
            constraints: "span 2,center",

            visible: true 
        )
      }
      
      panel( constraints: BorderLayout.PAGE_END,
          border: BorderFactory.createEmptyBorder( 0, 10, 10, 20 )
      ) {
        borderLayout( )
        panel( constraints: BorderLayout.LINE_END ) {
          btnOk = button( text: TXT_BUTTON_OK, 
              preferredSize: UI_Standards.BUTTON_SIZE,
              actionPerformed: { onButtonOk( ) } 
          )
          button( text: TXT_BUTTON_CANCEL,  
              preferredSize: UI_Standards.BUTTON_SIZE,
              actionPerformed: { onButtonCancel( ) }
          ) 
        }  
      }
    }
  }

  protected Boolean isMaximumDiscountEnabled( ) {
    return ( maximumDiscount > ZERO_TOLERANCE )
  }
  
  protected void setupTriggers( ) {
    trgDiscAmountLeave = new FocusAdapter( ) {
      public void focusLost( FocusEvent pEvent ) {
        DiscountCouponDialog.this.onDiscountAmountLeave()
      }
    }
    txtDiscountAmount.addFocusListener( trgDiscAmountLeave )
    
    trgDiscPercentLeave = new FocusAdapter( ) {
      public void focusLost( FocusEvent pEvent ) {
        DiscountCouponDialog.this.onDiscountPercentLeave()
      }
    }
    txtDiscountPercent.addFocusListener( trgDiscPercentLeave )
    
    if ( corporateEnabled ) {
      trgCorporateKeyLeave = new FocusAdapter( ) {
        public void focusLost( FocusEvent pEvent ) {
          DiscountCouponDialog.this.onCorporateKeyLeave( )
        }
      }
      txtCorporateKey.addFocusListener( trgCorporateKeyLeave )
      btnOk.setEnabled( false )
    }
    
  }

  protected void verifyCorporateKey( ) {
    if ( txtCorporateKey.getText( ).length( ) > 0 ) {
        descuentoClave = OrderController.descuentoClavexId(txtCorporateKey.text)
        if( descuentoClave == null ){
          descuentoClave = OrderController.descuentoClaveCupon(StringUtils.trimToEmpty(txtCorporateKey.text))//aqui
        }

        if (  descuentoClave != null ) {
            if(descuentoClave?.vigente == true){
                if(descuentoClave?.tipo != null && descuentoClave?.tipo.trim().equals('P')){
            txtDiscountPercent.setValue(descuentoClave?.porcenaje_descuento)
            txtDiscountAmount.setValue( txtDiscountPercent.getValue( ) * orderTotal / 100.0 )
                    porceLabel.setVisible(true)
                    porceText.setVisible(true)
                }else if(descuentoClave?.tipo != null && descuentoClave?.tipo.trim().equals('M')){
                    txtDiscountPercent.setValue(descuentoClave?.porcenaje_descuento)
                    txtDiscountAmount.setValue(  descuentoClave?.porcenaje_descuento)
                    porceLabel.setVisible(false)
                    porceText.setVisible(false)
                }
            lblStatus.text = TXT_VERIFY_PASS
        lblStatus.foreground = UI_Standards.NORMAL_FOREGROUND
        btnOk.setEnabled( true )

            } else {
                lblStatus.text = 'Descuento Inactivo'
                lblStatus.foreground = UI_Standards.WARNING_FOREGROUND
                btnOk.setEnabled( false )
            }
      } else {
        lblStatus.text = TXT_VERIFY_FAILED
        lblStatus.foreground = UI_Standards.WARNING_FOREGROUND
        btnOk.setEnabled( false )
      }
    } else {
      lblStatus.setText( "" )
      btnOk.setEnabled( false )
    }
  }
  
  protected void verifyDiscountConstraint( ) {
    Double maxAmountAllowed = orderTotal * maximumDiscount / 100.0
    Boolean verified = ( txtDiscountAmount.getValue( ) <= maxAmountAllowed )
    lblStatus.setText( 
      String.format( TXT_WARNING_MAX_AMOUNT, maximumDiscount, maxAmountAllowed )
    )
    lblStatus.setVisible( !verified )
    btnOk.setEnabled( verified )
  }
   
  // Public methods
  void activate( ) {
    discountSelected = false
    lblStatus.setText( "" )
    txtDiscountAmount.setValue( discountAmt.toDouble( ) )
    onDiscountAmountLeave( )
    this.setVisible( true )
  }
  
  String getCorporateKey( ) {
    String key = "0"
    if ( this.corporateEnabled ) {
      key = "2"
    }
    return key  
  }
  
  void setMaximumDiscount( Double pDiscountPercent ) {
    maximumDiscount = Math.abs( pDiscountPercent * 100 )
    if ( maximumDiscount < ZERO_TOLERANCE ) maximumDiscount = 0.0  
  }
  
  // UI Response
  void onButtonCancel() {
    setVisible( false )  
  }
  
  void onButtonOk() {
    CuponMv cuponMv = OrderController.obtenerCuponMvByClave(StringUtils.trimToEmpty(txtCorporateKey.text))
    if( cuponMv != null ){
      OrderController.updateCuponMvByClave(idOrder, cuponMv.claveDescuento)
    }
    discountSelected = true
    setDiscountAmt( txtDiscountAmount.getValue( ) )
    setDiscountPct( txtDiscountPercent.getValue( ) )
    setVisible( false ) 
  }
  
  void onDiscountAmountLeave( ) {
    txtDiscountPercent.setValue( 100.0 * txtDiscountAmount.getValue( ) / orderTotal )
    if ( !corporateEnabled && isMaximumDiscountEnabled( ) ) {
      verifyDiscountConstraint( )
    }
    if ( corporateEnabled ) {
      verifyCorporateKey( )
    }
  }
  
  void onDiscountPercentLeave( ) {
    txtDiscountAmount.setValue( txtDiscountPercent.getValue( ) * orderTotal / 100.0 )
    if ( !corporateEnabled && isMaximumDiscountEnabled( ) ) {
      verifyDiscountConstraint( )
    }
    if ( corporateEnabled ) {
      verifyCorporateKey( )
    }
  }
  
  void onCorporateKeyLeave( ) {
    txtCorporateKey.setText( txtCorporateKey.getText( ).toUpperCase( ) )
    verifyCorporateKey( )
  }
}
