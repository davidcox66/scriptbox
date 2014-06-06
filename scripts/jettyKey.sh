if [[ -z $1 ]] ; then
	echo "Usage: $0 <ssl hostname>"
	exit 1
fi
host="$1"
alias="jetty.${host}"
password="password"

set -x

if [[ -f $HOME/.scriptbox/keystore ]] ; then
	keytool -storepass "$password" -delete -alias "$alias" -keystore $HOME/.scriptbox/keystore 
fi

keytool -storepass "$password" -keystore $HOME/.scriptbox/keystore -alias "$alias" -genkey -keyalg RSA <<EOF
$host
Jetty




yes

EOF

