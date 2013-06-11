#
#
RELEASE=/soi/release
DISTRO=/soi/jpv/build/distributions
RESOURCES=/soi/jpv/core/src/main/resources
SOURCE=/soi/release/etc

# Read nextVersion 
VERSION=`cat $RELEASE/version`

# Prepare dir setup
CURRENT=$RELEASE/2.0$VERSION
if [ -d $CURRENT ]; then 
    rm -R $CURRENT
fi
mkdir -p $CURRENT/bin $CURRENT/etc $CURRENT/lib $CURRENT/local/prueba
mkdir -p $CURRENT/sql/lib $CURRENT/sql/out $CURRENT/sql/processed
mkdir -p $CURRENT/soi/lib $CURRENT/soi/etc $CURRENT/soi/local/prueba
mkdir -p $CURRENT/soi/sql/lib $CURRENT/soi/sql/processed $CURRENT/soi/sql/out  

# Prepare DISTRO
cd /soi/jpv
gradle dist
cd $DISTRO
unzip jpv-1.0.zip

# Full version 
cp -R jpv-1.0/* $CURRENT
echo "SOI 2.0$VERSION integrada el $(date)" > $CURRENT/etc/version
cp $CURRENT/etc/version $RELEASE/etc
cp $CURRENT/etc/version $CURRENT/local
cp $CURRENT/etc/version $CURRENT/local/prueba
cp $RELEASE/etc/*.xml $CURRENT/local
cp $RELEASE/etc/*.xml $CURRENT/local/prueba
cp $RELEASE/etc/soi.properties $CURRENT/local/database.properties
cp $RELEASE/etc/prueba.properties $CURRENT/local/prueba/database.properties
mv $CURRENT/bin/jpv $CURRENT/bin/soi 
mv $CURRENT/bin/jpv.bat $CURRENT/bin/soi.bat
cp $RELEASE/bin/Launcher.BAT $CURRENT/bin/jpv.bat
cp $RELEASE/bin/AfterDark.BAT $CURRENT/bin/AfterDark.BAT
cp $RELEASE/bin/BeforeDawn.BAT $CURRENT/bin/BeforeDawn.BAT
cp -R $RELEASE/sql/* $CURRENT/sql/
echo Current: $CURRENT

# Update only version
cp $CURRENT/lib/core-1.0.jar $CURRENT/lib/ui-1.0.jar $CURRENT/lib/reports-1.0.jar $CURRENT/lib/jpv-1.0.jar $CURRENT/soi/lib
cp $CURRENT/etc/version $CURRENT/soi/etc/version
cp $CURRENT/etc/version $CURRENT/soi/local
cp $CURRENT/etc/version $CURRENT/soi/local/prueba
cp $CURRENT/sql/lib/* $CURRENT/soi/sql/lib

# Update link to latest
rm /soi/release/latest
ln -s $CURRENT /soi/release/latest 

# Update version 
echo "SOI 2.0$VERSION+ desarrollando a partir de $(date)" > $RESOURCES/version
if [ "$1/A" == "/A"  ]; then
    VERSION=$((VERSION+1))
    echo $VERSION > $RELEASE/version
fi

# Clean distro
rm -R $DISTRO



