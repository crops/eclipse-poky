#!/bin/bash

#setup Yocto Eclipse plug-in build environment for Kepler
#comment out the following line if you wish to use your own http proxy settings
PROXY=http://proxy.jf.intel.com:911

err_exit() 
{
  echo "[FAILED $1]$2"
  exit $1
}

uname_s=`uname -s`
uname_m=`uname -m`

case ${uname_s}${uname_m} in
  Linuxppc*) ep_arch=linux-gtk-ppc
             cdt_arch=linux.ppc
             ;;
  Linuxx86_64*) ep_arch=linux-gtk-x86_64 
                cdt_arch=linux.x86_64
                ;;
  Linuxi*86) ep_arch=linux-gtk
             cdt_arch=linux.x86
             ;;
  *)
    echo "Unknown ${uname_s}${uname_m}"
    exit 1
    ;;
esac

#make sure that the utilities we need exist
command -v wget > /dev/null 2>&1 || { echo >&2 "wget not found. Aborting installation."; exit 1; }
command -v tar > /dev/null 2>&1 || { echo >&2 "tar not found. Aborting installation."; exit 1; }

#parsing proxy URLS
url=${PROXY}
if [ "x$url" != "x" ]; then
    proto=`echo $url | grep :// | sed -e 's,^\(.*://\).*,\1,g'`
    url=`echo $url | sed s,$proto,,g`
    userpass=`echo $url | grep @ | cut -d@ -f1`
    user=`echo $userpass | cut -d: -f1`
    pass=`echo $userpass | grep : | cut -d: -f2`
    url=`echo $url | sed s,$userpass@,,g`
    host=`echo $url | cut -d: -f1`
    port=`echo $url | cut -d: -f2 | sed -e 's,[^0-9],,g'`
    [ "x$host" = "x" ] && err_exit 1 "Undefined proxy host"
    PROXY_PARAM="-Dhttp.proxySet=true -Dhttp.proxyHost=$host"
    [ "x$port" != "x" ] && PROXY_PARAM="${PROXY_PARAM} -Dhttp.proxyPort=$port"
fi


# prepare the base Eclipse installation in folder "eclipse"
ep_rel="R-"
ep_ver="4.3"
ep_date="-201306052000"

if [ ! -f eclipse/plugins/3.102.0.v20130605-1539.jar ]; then
  pushd .
  if [ ! -d eclipse -o -h eclipse ]; then
    if [ -d eclipse-${ep_ver}-${ep_arch} ]; then
      rm -rf eclipse-${ep_ver}-${ep_arch}
    fi
    mkdir eclipse-${ep_ver}-${ep_arch}
    cd eclipse-${ep_ver}-${ep_arch}
  else
    rm -rf eclipse
  fi

  echo -e "\nPlease wait. Downloading Eclipse SDK ${ep_rel}${ep_ver}${ep_date} \n"

  if [[ "$1" = "--upstream" ]]
  then
        wget "http://archive.eclipse.org/eclipse/downloads/drops4/${ep_rel}${ep_ver}${ep_date}/eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz"
  else
	wget "http://downloads.yoctoproject.org/eclipse/downloads/drops4/${ep_rel}${ep_ver}${ep_date}/eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz"
  fi

  echo -e "Please wait. Extracting Eclipse SDK: eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz\n"

  tar xfz eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz || err_exit $? "extracting Eclipse SDK failed"

  rm eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz
  popd

  if [ ! -d eclipse -o -h eclipse ]; then
    if [ -e eclipse ]; then 
      rm eclipse
    fi
    ln -s eclipse-${ep_ver}-${ep_arch}/eclipse eclipse
  fi
fi

if [ ! -f eclipse/startup.jar ]; then
  pushd .
  cd eclipse/plugins
  if [ -h ../startup.jar ]; then
    rm ../startup.jar
  fi
  LAUNCHER="`ls org.eclipse.equinox.launcher_*.jar | sort | tail -1`"
  if [ "x${LAUNCHER}" != "x" ]; then
    echo "eclipse LAUNCHER=${LAUNCHER}" 
    ln -s plugins/${LAUNCHER} ../startup.jar
  else
    echo "Eclipse: NO startup.jar LAUNCHER FOUND!"
  fi
  popd
fi

LAUNCHER="eclipse/startup.jar"

#$1: repository_url
#$2: featureId
#$3: 'all' or 'max' or 'min', 'max' if not specified
get_version()
{
  local remote_vers="`java ${PROXY_PARAM} \
    -jar ${LAUNCHER} \
    -application org.eclipse.equinox.p2.director \
    -destination ./eclipse \
    -profile SDKProfile \
    -repository $1 \
    -list $2\
    | awk 'BEGIN { FS="=" } { print $2 }'`"

  #find larget remote vers
  local remote_ver="`echo ${remote_vers} | cut -d ' ' -f1`"
  case $3 in
    all)
      remote_ver=${remote_vers}
      ;;
    min)
      for i in ${remote_vers}; do
        [ "${remote_ver}" \> "$i" ] && remote_ver="$i"
      done
      ;;
    *)
      for i in ${remote_vers}; do
        [ "${remote_ver}" \< "$i" ] && remote_ver="$i"
      done
      ;;
  esac

  echo ${remote_ver}
}

# $1 unitId
# $2 min version
# $3 max version (optional)
check_local_version()
{
  curdir=`pwd`
  version="`get_version file:///${curdir}/eclipse/p2/org.eclipse.equinox.p2.engine/profileRegistry/SDKProfile.profile $1`"
  [ "$version" \< "$2" ] && return 1
  if [ "x$3" != "x" ]; then
    [ "$version" \> "$3" ] && return -1
  fi
  return 0
}


# install a feature with version requirement [min, max)
#$1: reporsitory url
#$2: featureId
#$3: min version
#$4: max version(optional)
update_feature_remote()
{
  [ $# -lt 3 ] && err_exit 1 "update_feature_remote: invalid parameters, $*"
  check_local_version $2 $3 $4 && echo "Feature $2 is already installed" && return 0
  local installIU=""
  if [ "x$4" != "x" ]; then
      #has max version requirement
      for i in "`get_version $1 $2 'all'`"; do
        if [ "$i" \> "$3" ] || [ "$i" = "$3" ] && [ "$i" \< "$4" ]; then
          [ "$i" \> "$installIU" ] && installIU=$i
        fi
      done
  else
      #only has minimum version requirement
      local max_remote_ver="`get_version $1 $2 'max'`"
      [ "$max_remote_ver" \> "$3" ] || [ "$max_remote_ver" = "$3" ] && installIU=$max_remote_ver
  fi

  [ "x$installIU" = "x" ] && err_exit 1 "Can NOT find candidates of $2 version($3, $4) at $1!"
  installIU="$2/$installIU"
  java ${PROXY_PARAM} -jar ${LAUNCHER} \
    -application org.eclipse.equinox.p2.director \
    -destination ${curdir}/eclipse \
    -profile SDKProfile \
    -repository $1 \
    -installIU ${installIU} || err_exit $? "installing ${installIU} failed"
}

#Main Site
if [[ "$1" = "--upstream" ]]
then
        MAIN_SITE="http://download.eclipse.org/releases/kepler"
else
        MAIN_SITE="http://downloads.yoctoproject.org/eclipse/kepler/ftp.osuosl.org/pub/eclipse/releases/kepler"
fi

CDTFEAT="8.3.0"
echo -e "\nPlease wait. Installing CDT.SDK.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.cdt.feature.group ${CDTFEAT}


CDTREMOTEVER="6.2.0"
echo -e "\nPlease wait. Installing CDT.LAUNCH.REMOTE.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.cdt.launch.remote.feature.group ${CDTREMOTEVER}

RSEVER="3.5.0"
echo -e "\nPlease wait. Installing RSE.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.rse.feature.group ${RSEVER}

TMVER="3.3.1"
echo -e "\nPlease wait. Installing TM.TERMINAL.SDK.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.tm.terminal.sdk.feature.group ${TMVER}

ATVER="3.2.0"
echo -e "\nPlease wait. Installing CDT.AUTOTOOLS.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.cdt.autotools.feature.group ${ATVER}

LTTNGREL="2.0.0"
echo -e "\nPlease wait. Installing LINUXTOOLS.LTTNG2.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.linuxtools.lttng2.feature.group ${LTTNGREL}

PTPVER="7.0.0"
echo -e "\nPlease wait. Installing PTP.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.ptp.feature.group ${PTPVER}

RDTVER="7.0.0"
echo -e "\nPlease wait. Installing PTP.RDT.FEATURE.GROUP"
update_feature_remote ${MAIN_SITE} org.eclipse.ptp.rdt.feature.group ${RDTVER}

echo -e "\nYour build environment is successfully created."
echo -e "\nRun ECLIPSE_HOME=`pwd`/eclipse `dirname $0`/build.sh <plugin branch or tag name> <documentation branch or tag name> <release name> to build the plugin and its documentation\n"

exit 0
