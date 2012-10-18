set -x
export JAVA_OPTS="\
		-Dlogback.debug=true \
		-Dmetrics.level=TRACE"

$TOMCAT_HOME/bin/shutdown.sh
rm -rf $TOMCAT_HOME/webapps/panopticon*
cp $(dirname $0)/../target/panopticon.ui-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/panopticon.war
sleep 10
rm -rf $TOMCAT_HOME/logs/*
$TOMCAT_HOME/bin/startup.sh

