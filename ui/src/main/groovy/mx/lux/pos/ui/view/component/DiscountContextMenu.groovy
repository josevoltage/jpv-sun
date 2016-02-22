package mx.lux.pos.ui.view.component

import groovy.swing.SwingBuilder
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.model.OrderItem
import mx.lux.pos.ui.view.driver.PromotionDriver
import org.apache.commons.lang3.StringUtils

import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class DiscountContextMenu extends JPopupMenu {

  private SwingBuilder sb = new SwingBuilder( )
  private PromotionDriver driver
  private JMenuItem menuDiscount
  private JMenuItem menuCorporateDiscount
  private JMenuItem menuWarrantyDiscount
  private JMenuItem menuCouponDiscount

  DiscountContextMenu( PromotionDriver pDriver ) {
    driver = pDriver
    buildUI( )
  }
  
  protected buildUI( ) {
    sb.popupMenu( this ) {
      menuDiscount = menuItem( text: "Descuento Tienda",
        visible: Registry.activeStoreDiscount,
        actionPerformed: { onDiscountSelected( ) },
      )
      menuCorporateDiscount = menuItem( text: "Descuento Corporativo", 
        visible: true,
        actionPerformed: { onCorporateDiscountSelected( ) },
      )
        menuWarrantyDiscount = menuItem( text: "Garantia",
        visible: true,
        actionPerformed: { onWarrantyDiscountSelected( ) },
      )
      menuCouponDiscount = menuItem( text: "Descuento por Cupon",
        visible: true,
        actionPerformed: { onCouponDiscountSelected( ) },
      )
    }
  }
  
  // Public Methods
  void activate( MouseEvent pEvent ) {
    menuDiscount.setEnabled( driver.isDiscountEnabled( ) )
    menuCorporateDiscount.setEnabled( driver.isCorporateDiscountEnabled( ) )
    menuCouponDiscount.setEnabled( driver.isCorporateDiscountEnabled( ) && validToCupon() )
    menuWarrantyDiscount.setEnabled( driver.isCorporateDiscountEnabled( ) && validToWarranty() )
    show( pEvent.getComponent(), pEvent.getX(), pEvent.getY() )
  } 
  
  // UI Response
  protected void onDiscountSelected( ) {
    driver.requestDiscount( true, true )
  }
  
  protected void onCorporateDiscountSelected( ) {
    driver.requestCorporateDiscount( false, false )
  }

  protected void onWarrantyDiscountSelected(){
    driver.requestWarrantyDiscount()
  }

  protected void onCouponDiscountSelected(){
    driver.requestCouponDiscount()
  }

  Boolean validToWarranty(){
    Boolean valid = false
    if( driver.view.promotionListSelected.size() <= 0 ){
      Integer count = 0
      for(OrderItem tmp : driver.view.order.items){
        if( StringUtils.trimToEmpty(tmp.item.type).equalsIgnoreCase("A") ){
          count = count+1
        }
      }
      if( count == 1 ){
        valid = true
      }
    }
    return  valid
  }


  Boolean validToCupon(){
    Boolean valid = false
    if( driver.view.promotionListSelected.size() <= 0 ){
      valid = true
    }
    return  valid
  }

}
