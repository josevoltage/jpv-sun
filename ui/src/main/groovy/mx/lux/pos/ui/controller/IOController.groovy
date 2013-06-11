package mx.lux.pos.ui.controller

import mx.lux.pos.model.Sucursal
import mx.lux.pos.ui.resources.ServiceManager
import mx.lux.pos.ui.view.dialog.ImportPartMasterDialog
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import mx.lux.pos.service.IOService
import mx.lux.pos.ui.model.file.FileFilteredList
import mx.lux.pos.ui.model.file.FileFiltered
import mx.lux.pos.ui.view.dialog.ImportClasificationArticleDialog
import mx.lux.pos.ui.model.file.DateFileFiltered
import java.text.SimpleDateFormat
import org.apache.commons.lang.StringUtils


class IOController {

    private Logger log = LoggerFactory.getLogger(this.getClass())
    private static IOController instance

    private IOController() { }

    static IOController getInstance() {
        if (instance == null) {
            instance = new IOController()
        }
        return instance
    }

    void requestImportPartMaster() {
        log.debug(String.format('Request ImportPartMaster'))
        ImportPartMasterDialog dialog = new ImportPartMasterDialog()
        dialog.setFilenamePattern(ServiceManager.ioServices.productsFilePattern)
        dialog.activate()
    }

    void requestImportClasificationArtMaster() {
        log.debug(String.format('Request ImportClasificationArtMaster'))
        ImportClasificationArticleDialog dialog = new ImportClasificationArticleDialog()
        dialog.setFilenamePattern( ServiceManager.ioServices.clasificationsFilePattern )
        dialog.activate()
    }


    void dispatchImportPartMaster(File pFile) {
        ServiceManager.ioServices.loadPartFile(pFile)
    }

    void autoUpdateEmployeeFile() {
        this.log.debug('AutoUpdate EmployeeFile')
        String pattern = ServiceManager.ioServices.getEmployeeFilePattern()
        FileFilteredList list = new FileFilteredList(pattern)
        File incomingPath = ServiceManager.ioServices.getIncomingLocation()
        for (File f : incomingPath.listFiles()) {
            list.add(f)
        }
        File f = list.pop()
        while (f != null) {
            ServiceManager.ioServices.loadEmployeeFile(f)
            f = list.pop()
        }
    }

    void autoUpdateFxRates() {
        this.log.debug('AutoUpdate FxRates')
        String pattern = ServiceManager.ioServices.getFxRatesFilePattern()
        FileFilteredList list = new FileFilteredList(pattern)
        File incomingPath = ServiceManager.ioServices.getIncomingLocation()
        for (File f : incomingPath.listFiles()) {
            list.add(f)
        }
        File f = list.pop()
        while (f != null) {
            ServiceManager.ioServices.loadFxRatesFile(f)
            f = list.pop()
        }
    }

    void startAsyncNotifyDispatcher() {
        this.log.debug('Trigger Async Notification Dispatcher')
        ServiceManager.ioServices.startAsyncNotifyDispatcher()
    }


    void dispatchImportClasificationArt(File pFile) {
        this.log.debug( 'Importando Clasificacion de Articulos' )
        Map<String, Object> importSummary = ServiceManager.ioServices.loadPartClassFile( pFile )
    }


    Boolean validateCentroCostos( String centroCostos ){
        Boolean valid = false
        Sucursal sucActual = ServiceManager.storeService.obtenSucursalActual()
        if( !sucActual.centroCostos.trim().equalsIgnoreCase(centroCostos.trim()) ){
            valid = true
        }
        return valid
    }
}

