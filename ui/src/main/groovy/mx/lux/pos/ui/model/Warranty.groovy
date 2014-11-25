package mx.lux.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.lux.pos.model.Cliente

@Bindable
@ToString
@EqualsAndHashCode
class Warranty {
  BigDecimal amount
  Integer idItem


}
