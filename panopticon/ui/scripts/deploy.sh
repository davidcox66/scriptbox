set -x
$TOMCAT_HOME/bin/shutdown.sh
rm -rf $TOMCAT_HOME/webapps/panopticon*
cp $(dirname $0)/../target/panopticon.ui-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/panopticon.war
sleep 10
$TOMCAT_HOME/bin/startup.sh

