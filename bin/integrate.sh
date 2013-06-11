#
#
BUILD_PATH=/soi/build
SOI_PATH=/soi/jpv
DISTRO_PATH=/soi/jpv/build/distributions
RESOURCE_DIR=/soi/jpv/core/src/main/resources

# Read nextBuild
BUILD=`cat $SOI_PATH/build.seq`

# Prepare dir setup
CURRENT=$BUILD_PATH/$BUILD
if [ -d $CURRENT ]; then 
    rm -R $CURRENT
fi
mkdir -p $CURRENT/bin $CURRENT/etc $CURRENT/lib $CURRENT/sql

# Prepare DISTRO
cd $SOI_PATH
gradle dist
cd $DISTRO_PATH
unzip jpv-1.0.zip

# Full version 
cp -R jpv-1.0/* $CURRENT
echo "SOI BUILD #$BUILD integrada el $(date)" > $CURRENT/etc/version
cp $RESOURCE_DIR/sql/* $CURRENT/sql
echo Current: $CURRENT

# Update link to latest
rm $BUILD_PATH/latest
ln -s $CURRENT $BUILD_PATH/latest 

# Update version 
if [ "$1/A" == "/A"  ]; then
    BUILD=$((BUILD+1))
    echo $BUILD > $SOI_PATH/build.seq
fi
echo "SOI BUILD #$BUILD(+) desarrollando a partir de $(date)" > $RESOURCE_DIR/version

# Clean distro
rm -R $DISTRO_PATH



