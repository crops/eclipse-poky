#! /bin/sh
[ -e config.cache ] && rm -f config.cache

libtoolize -f --automake -c
aclocal ${OECORE_ACLOCAL_OPTS}
autoconf
autoheader
automake -a -c
./configure $@
exit
