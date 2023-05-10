#!/bin/bash
if [ -f "tibjms.jar" ]; then exit 0; fi
rm -rf /tmp/tibco
mkdir /tmp/tibco
curl -s -o /tmp/tibco/tibco.zip "https://edownloads.tibco.com/Installers/tap/EMS-CE/10.2.1/TIB_ems-ce_10.2.1_linux_x86_64.zip?SJCDPTPG=1686307326_ef68d106a432fba5bdbcb60665c8b2b1&ext=.zip"
cd /tmp/tibco
unzip tibco.zip TIB_ems-ce_10.2.1/tar/TIB_ems-ce_10.2.1_linux_x86_64-java_client.tar.gz
tar -zxf TIB_ems-ce_10.2.1/tar/TIB_ems-ce_10.2.1_linux_x86_64-java_client.tar.gz  opt/tibco/ems/10.2/lib/tibjms.jar
cd -
cp /tmp/tibco/opt/tibco/ems/10.2/lib/tibjms.jar .