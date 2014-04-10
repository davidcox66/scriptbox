dir=$(dirname $0)
remote_dir=$1
shift 1

set -x
for host in $* ; do
	ssh $host mkdir -p $remote_dir
	#scp $dir/monitor $dir/monitorcli $dir/monitor.server $dir/../build/distributions/panopticon-*-standalone.jar $host:$remote_dir
	scp $dir/monitor $dir/monitorcli $dir/monitor.server $host:$remote_dir
done
