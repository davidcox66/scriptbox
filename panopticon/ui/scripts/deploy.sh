set -x
scp $(dirname $0)/../target/captcha.gwt-1.0-SNAPSHOT.war gandolf:/tmp
ssh remote_host "\
  sudo /opt/tomcat/bin/shutdown.sh ; \
  sudo cp /tmp/panopticon.gwt-1.0-SNAPSHOT.war /opt/tomcat/webapps/panopticon.war ; \
  sudo /opt/tomcat/bin/startup.sh"
