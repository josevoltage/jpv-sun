package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.model.ICorporateKeyVerifier
import mx.lux.pos.ui.model.Item
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
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat

class CouponDiscountDialog extends JDialog {

  private static final String TXT_DISCOUNT_TITLE = "Aplicar descuento en tienda"
  private static final String TXT_CORPORATE_TITLE = "Aplicar descuento corporativo"
  private static final String TXT_AMOUNT_LABEL = "Monto"
  private static final String TXT_CORPORATE_KEY_LABEL = "Clave Descuento"
  private static final String TXT_PERCENT_LABEL = "%Descuento"
  private static final String TXT_BUTTON_CANCEL = "Cancelar"
  private static final String TXT_BUTTON_OK = "Aplicar"
  private static final String TXT_WARNING_MAX_AMOUNT = "Límite de descuento en tienda: %.1f%% (%,.2f)"
  private static final String TXT_VERIFY_PASS = ""
  private static final String TXT_VERIFY_FAILED = "Clave incorrecta"

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

  ICorporateKeyVerifier verifier
  Double orderTotal = 0
  Double maximumDiscount = 0
  Boolean corporateEnabled = false
  BigDecimal discountAmt = 0
  Double discountPct = 0
  Boolean discountSelected
  Boolean authorizationManager = false
  Boolean needAuthorization = false
  Item item
  String clave

  CouponDiscountDialog( Item item ) {
    this.needAuthorization = needAuthorization
    this.authorizationManager = authorizationManager
    this.item = item
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
        title: ( "Clave Descuento" ) ,
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
        label( TXT_CORPORATE_KEY_LABEL, font: bigLabel )
        txtCorporateKey = textField( font: bigInput,
              horizontalAlignment: JTextField.LEFT,
              actionPerformed: { onCorporateKeyLeave( ) }
        )
        txtCorporateKey.addFocusListener( new FocusListener() {
            @Override
            void focusGained(FocusEvent e) {
              verifyCorporateKey()
            }

            @Override
            void focusLost(FocusEvent e) {
              verifyCorporateKey()
            }
        })
        label( TXT_AMOUNT_LABEL, font: bigLabel )
        textField( txtDiscountAmount,
            font: bigInput,
            horizontalAlignment: JTextField.LEFT,
            editable: false
            //actionPerformed: { onDiscountAmountLeave( ) }
        )
        lblStatus = label( TXT_WARNING_MAX_AMOUNT,
            foreground: UI_Standards.WARNING_FOREGROUND,
            constraints: "span 2,center",
            visible: false
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
        CouponDiscountDialog.this.onDiscountAmountLeave()
      }
    }
    txtDiscountAmount.addFocusListener( trgDiscAmountLeave )
    
    trgDiscPercentLeave = new FocusAdapter( ) {
      public void focusLost( FocusEvent pEvent ) {
        CouponDiscountDialog.this.onDiscountPercentLeave()
      }
    }
    txtDiscountPercent.addFocusListener( trgDiscPercentLeave )

    if ( corporateEnabled ) {
      trgCorporateKeyLeave = new FocusAdapter( ) {
        public void focusLost( FocusEvent pEvent ) {
          CouponDiscountDialog.this.onCorporateKeyLeave( )
        }
      }
      txtCorporateKey.addFocusListener( trgCorporateKeyLeave )
      btnOk.setEnabled( false )
    }
    
  }

  protected void verifyCorporateKey( ) {
    if ( txtCorporateKey.getText( ).length( ) > 0 ) {
      if ( requestVerify( ) != null ) {
        lblStatus.text = TXT_VERIFY_PASS
        lblStatus.foreground = UI_Standards.NORMAL_FOREGROUND
        btnOk.setEnabled( true )
        lblStatus.visible = false
      } else {
        if( StringUtils.trimToEmpty(lblStatus.text).length() <= 0 ){
          lblStatus.text = TXT_VERIFY_FAILED
        }
        lblStatus.visible = true
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
    String key = ""
    key = StringUtils.trimToEmpty(this.clave)
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

  String requestVerify( ){
    SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
    SimpleDateFormat formatter2 = new SimpleDateFormat("yy");
    String clave = ""
    if( StringUtils.trimToEmpty(txtCorporateKey.text).length() >= 11 &&
            StringUtils.trimToEmpty(txtCorporateKey.text).toUpperCase().startsWith("C")){
      for(int i=0;i<StringUtils.trimToEmpty(txtCorporateKey.text).length();i++){
        if(StringUtils.trimToEmpty(txtCorporateKey.text.toUpperCase().charAt(i).toString()).isNumber()){
          Integer number = 0
          try{
            number = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(txtCorporateKey.text.toUpperCase().charAt(i).toString()))
          } catch ( NumberFormatException e ) { println e }
          clave = clave+StringUtils.trimToEmpty((10-number).toString())
        } else {
          clave = clave+0
        }
      }
      clave = clave.replaceFirst("0","C")
      String dateStr = StringUtils.trimToEmpty(clave).substring(6,10)
      String amountStr = StringUtils.trimToEmpty(clave).substring(3,5)
      Date date = new Date()
      BigDecimal amount = BigDecimal.ZERO
      dateStr = dateStr+StringUtils.trimToEmpty(formatter2.format(new Date()))
      try{
        date = formatter.parse(dateStr)
        amount = NumberFormat.getInstance().parse(amountStr)
      } catch ( ParseException e) {
        e.printStackTrace()
      } catch ( NumberFormatException e) {
          e.printStackTrace()
      }
      Boolean keyFree = OrderController.keyFree(StringUtils.trimToEmpty(clave).toUpperCase())
      if( date.compareTo(new Date()) >= 0 ){
        if( keyFree ){
          BigDecimal montoMinimo = new BigDecimal(Registry.minimumAmountApplyCoupon)
          println "monto minimo para aplicar cupon: ${montoMinimo}"
          if( item.price.compareTo(montoMinimo) >= 0 ){
            println clave.substring(5,6)
            if( clave.substring(5,6).equalsIgnoreCase("1") ){
              txtDiscountAmount.setText( StringUtils.trimToEmpty((new BigDecimal(amount).multiply(new BigDecimal(10))).toString()) )
            } else {
              txtDiscountAmount.setText( StringUtils.trimToEmpty((item.price.multiply(new BigDecimal(amount/100))).toString()) )
            }
            this.clave = clave
          } else {
            NumberFormat nf = NumberFormat.getCurrencyInstance( Locale.US )
            lblStatus.setText("El monto minimo de la nota debe ser de ${nf.format(montoMinimo)}")
            clave = null
            this.clave = ""
          }
        } else {
          lblStatus.setText("Clave ya aplicada")
          clave = null
          this.clave = ""
        }
      } else {
        lblStatus.setText("La clave no es vigente")
        clave = null
        this.clave = ""
      }
    } else {
      lblStatus.setText("Formato de clave incorrecto")
      clave = null
      this.clave = ""
    }
    return clave
  }


}
