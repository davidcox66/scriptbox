set -x
export TOMCAT_HOME=/Users/david/david/tools/tomcat
$TOMCAT_HOME/bin/shutdown.sh
rm -rf $TOMCAT_HOME/webapps/panopticon*
cp $(dirname $0)/../target/panopticon.gwt-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/panopticon.war
$TOMCAT_HOME/bin/startup.sh

