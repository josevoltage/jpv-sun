package mx.lux.pos.service

import com.sun.jna.Library
import com.sun.jna.Native
import mx.lux.pos.model.*

interface CLibrary extends Library{

    CLibrary INSTANCE = (CLibrary) Native.loadLibrary("C:/Apps/appBanc/uPaydll.dll", CLibrary.class);
    int getpid();
    int getppid();
    //long time(long buf[]);


}
