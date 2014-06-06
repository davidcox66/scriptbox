if [[ -z $1 ]] ; then
	echo "Usage: $0 <ssl hostname> [JAVA_HOME]"
	exit 1
fi

host="$1"
alias="jetty.${host}"
password="password"

if [[ -n $2 ]] ; then
	export JAVA_HOME=$2
fi

set -x

keytool -storepass "$password" -keystore $HOME/.scriptbox/keystore -exportcert -alias "$alias" -file public.cert 
keytool -storepass "$password" -delete -alias "$alias" -keystore $HOME/.scriptbox/truststore 
keytool -storepass "$password" -import -alias "$alias" -keystore $HOME/.scriptbox/truststore -file public.cert <<EOF
yes
EOF

rm -f public.cert
