#!/bin/bash

#setup Yocto Eclipse plug-in build environment for Neon.1
#comment out the following line if you wish to use your own http proxy settings
#export http_proxy=http://proxy.yourproxyinfo.com:8080

help ()
{
  echo -e "\nThis script sets up the Yocto Project Eclipse plugins build environment"
  echo -e "All files are downloaded from the Yocto Project mirror by default\n"
  echo -e "Usage: $0 [-u|--upstream] [-q|--quiet]\n";
  echo "Options:"
  echo -e "--upstream - download from the upstream Eclipse repository\n"
  echo -e "--quiet - suppress installation trace\n"
  echo -e "Example: $0 --upstream\n";
  exit 1;
}

err_exit()
{
  echo "[FAILED $1] $2"
  exit $1
}

OPTS="$@"
GETOPT=`getopt -o huq --long help,upstream,quiet -n 'setup.sh' -- $OPTS`
STATUS=$?

[ $STATUS -eq 0 ] || err_exit $STATUS "Problem parsing options: $OPTS"

eval set -- "$GETOPT"

# By default, do not download Eclipse archives or features from
# upstream mirrors
USE_UPSTREAM=0

# Enable P2 debug traces by default
P2_INSTALL_TRACE=1

while true; do
  case $1 in
    -h|--help) help; shift;;
    -u|--upstream) USE_UPSTREAM=1; shift;;
    -q|--quiet) P2_INSTALL_TRACE=0; shift;;
    --) shift; break;;
    *) break;;
  esac
done

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
url=${http_proxy}
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

echo "#### Checking that Java is available ####"
if type -p java; then
    echo java found in PATH
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    echo java found in JAVA_HOME
    _java="$JAVA_HOME/bin/java"
else
    err_exit 2 "no java"
fi
echo "#### Checking that the appropriate Java version is available ####"
JAVA_VER_STRING=1.8
JAVA_VER_INT=001008
if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo java version as string = $version
    version_int=$(echo "$version" | awk -F. '{printf("%03d%03d",$1,$2);}')
    echo java version as integer = $version_int
    if [ $version_int -lt $JAVA_VER_INT ]; then
        err_exit 1 "Java version must be $JAVA_VER_STRING+"
    fi
fi

# prepare the base Eclipse installation in folder "eclipse"
ep_rel="R-"
ep_ver="4.8"
ep_date="-201806110500"
P2_disabled=false
P2_no_dropins=false


if [ ! -f eclipse/plugins/org.eclipse.swt_3.107.0.v20180611-0422.jar ]; then

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

  # Eclipse SDK: Need the SDK so we can link into docs
  echo -e "\nPlease wait. Downloading Eclipse SDK ${ep_rel}${ep_ver}${ep_date} \n"

  if [ $USE_UPSTREAM -eq 1 ]
  then
        wget "http://download.eclipse.org/eclipse/downloads/drops4/${ep_rel}${ep_ver}${ep_date}/eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz"
  else
        wget "http://downloads.yoctoproject.org/eclipse-full/eclipse/downloads/drops4/${ep_rel}${ep_ver}${ep_date}/eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz"
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

  # Disable P2 mirrors if we're not using upstream URLs to download
  # Eclipse artifacts and features. The following wiki page suggests
  # adding -vmargs -Declipse.p2.mirrors=false at the end of the Java
  # command line, in our case this means when the P2 director being
  # invoked for feature installation:
  #
  #   https://wiki.eclipse.org/Equinox/p2/p2.mirrorsURL#Avoiding_mirrors.2C_even_when_using_p2.mirrorsURL
  #
  # Unfortunate this doesn't seem to work, so instead we modify the
  # generated config.ini to force P2 to not use mirrors. This should be
  # fine for as long as the eclipse installation directory which
  # contains the modified config.ini is only used for building the
  # plugins.

  [ $USE_UPSTREAM -eq 0 ] && echo "eclipse.p2.mirrors=false" >> eclipse/configuration/config.ini

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

# install a feature with version requirement [min, max]
#$1: reporsitory url
#$2: featureId
#$3: min version
#$4: max version(optional)
update_feature_remote()
{
  [ $# -lt 3 ] && err_exit 1 "update_feature_remote: invalid parameters, $*"
  echo -e "\nPlease wait. Installing $2 $3 $4"
  check_local_version $2 $3 $4 && echo "Feature $2 is already installed" && return 0
  local installIU=""
  local all_versions=$(get_version $1 $2 'all')

  echo "Feature $2 versions available: $all_versions"

  if [ "x$4" != "x" ]; then
      #has max version requirement
      for i in $all_versions; do
        if [ "$i" \> "$3" ] || [ "$i" = "$3" ] && [ "$i" \< "$4" ]; then
          [ "$i" \> "$installIU" ] && installIU=$i
        fi
      done
  else
      #only has minimum version requirement
      for i in $all_versions; do
        if [ "$i" \> "$3" ] || [ "$i" = "$3" ]; then
          installIU=$i
          break
        fi
      done
  fi

  [ "x$installIU" = "x" ] && err_exit 1 "Can NOT find candidates of $2 version($3, $4) at $1!"
  [ "$P2_INSTALL_TRACE" == "1" ] && TRACE_OPTS="-debug ./trace.ini"

  installIU="$2/$installIU"
  java ${PROXY_PARAM} -jar ${LAUNCHER} \
    $TRACE_OPTS \
    -application org.eclipse.equinox.p2.director \
    -destination ./eclipse \
    -profile SDKProfile \
    -repository $1 \
    -installIU ${installIU} || err_exit $? "installing ${installIU} failed"
}

#Main Site
if [ $USE_UPSTREAM -eq 1 ]
then
        MAIN_SITE="http://download.eclipse.org/releases/photon"
        TM_SITE="http://download.eclipse.org/tm/updates/3.7.100/repository/"
        TM_TERMINAL_SITE="http://download.eclipse.org/tm/terminal/updates/4.4milestones/20180611/"
        CDT_SITE="http://download.eclipse.org/tools/cdt/releases/9.5/"
else
        MAIN_SITE="http://downloads.yoctoproject.org/eclipse-full/releases/photon/"
        TM_SITE="http://downloads.yoctoproject.org/eclipse-full/tm/updates/3.7.100/repository/"
        TM_TERMINAL_SITE="http://downloads.yoctoproject.org/eclipse-full/tm/terminal/updates/4.4milestones/20180611/"
        CDT_SITE="http://downloads.yoctoproject.org/eclipse-full/tools/cdt/releases/9.5/"
fi

#Update Site - We have a full mirror of eclipse now, so default to that
if [ $USE_UPSTREAM -eq 1 ]
then
        UPDATE_SITE="http://download.eclipse.org/eclipse/updates/4.8"
else
        UPDATE_SITE="http://downloads.yoctoproject.org/eclipse-full/eclipse/updates/4.8/"
fi

# Features are organized by simrel or their respective update sites. Except CDT
# which might be installed from it's update site to get the latest features and
# bug fixes, most other features which eclipse-yocto do not directly depend on
# should be installed from simrel whenever possible so that the feature's
# dependencies can be automatically resolved.

# It is also worth pointing out that the installation order do not matter when
# all features are installed from simrel, however features which are installed
# from it's update site would require it's dependencies to be installed first.

# Note that for CDT, only either one of the features from simrel or update site
# can be installed, so be sure to only uncomment one of the following groups of
# lines below to avoid p2 error which complains about newer version of the IU
# cannot be installed, as describe here:
#
#   http://wiki.eclipse.org/Equinox/p2/FAQ#Why_am_I_getting_dependency_satisfaction_errors_when_I_update_my_feature.3F
#
# Alternatively, an approach which might be more similar to using the Eclipse
# Update Manager would be to implement the ability to invoke p2 to uninstall
# the prior version.

# CDT features from simrel
# Uncomment this to install from simrel
#update_feature_remote ${MAIN_SITE} org.eclipse.cdt.sdk.feature.group 9.5.0
#update_feature_remote ${MAIN_SITE} org.eclipse.cdt.launch.remote.feature.group 9.5.0
#update_feature_remote ${MAIN_SITE} org.eclipse.cdt.autotools.feature.group 9.5.0

# PTP features from simrel
# This is needed for installing CDT from it's update site.
update_feature_remote ${MAIN_SITE} org.eclipse.remote.feature.group 3.0.0

# TM Terminal features from simrel
update_feature_remote ${MAIN_SITE} org.eclipse.tm.terminal.feature.feature.group 4.4.0
update_feature_remote ${MAIN_SITE} org.eclipse.tm.terminal.control.feature.feature.group 4.4.0

# Trace Compass features from simrel
update_feature_remote ${MAIN_SITE} org.eclipse.tracecompass.lttng2.ust.feature.group 4.0.0

# RSE features from update site
update_feature_remote ${TM_SITE} org.eclipse.rse.sdk.feature.group 3.7.3
update_feature_remote ${TM_SITE} org.eclipse.rse.terminals.feature.group 3.8.0

# CDT features from update site
# Uncomment this to install from update site
update_feature_remote ${CDT_SITE} org.eclipse.cdt.sdk.feature.group 9.5.2
update_feature_remote ${CDT_SITE} org.eclipse.cdt.launch.remote.feature.group 9.5.2
update_feature_remote ${CDT_SITE} org.eclipse.cdt.autotools.feature.group 9.5.2

# TM Terminal features from update site
update_feature_remote ${TM_TERMINAL_SITE} org.eclipse.tm.terminal.view.rse.feature.feature.group 4.3.0

echo -e "\nYour build environment is successfully created."
echo -e "\nPlease execute the following command to build the plugins and their documentation."
echo -e "\nThe build log will be stored at `pwd`/build.log."

echo -e "\nECLIPSE_HOME=`pwd`/eclipse `dirname $0`/build.sh <plugin branch or tag name> <documentation branch or tag name> <release name> 2>&1 | tee -a build.log\n"

exit 0
