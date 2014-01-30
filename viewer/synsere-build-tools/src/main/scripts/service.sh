#!/bin/bash

dir=$(readlink -f $(dirname $0))
file="config.js"
config="file://$dir/$file"
java_home=$(dirname $(dirname $(readlink -f `which java`)))
jsvc=`which jsvc`
var="$dir/var"
log="$dir/log"

pidfile="$var/${project.artifactId}.pid"
outfile="$log/${project.artifactId}.out"
errfile="$log/${project.artifactId}.err"

mkdir -p $var
mkdir -p $log

cd $dir

jar="${project.artifactId}-${project.version}.jar"

delegate()
{
    options=(
      # jsvc args
      -home "$java_home"
      -cp "$jar"
      -pidfile "$pidfile"
      -outfile "$outfile"
      -errfile "$errfile"
      -wait 10
      # either nothing or -start
      $1
      # jvm args
      -Xmx384m
      -Xms256m
      -XX:+UseConcMarkSweepGC
      -XX:+UseParNewGC
      -XX:ParallelGCThreads=2
      -Djava.naming.provider.url="$config"
#      -Dcom.sun.management.jmxremote
#      -Dcom.sun.management.jmxremote.authenticate=false
#      -Dcom.sun.management.jmxremote.port=9901
#      -Dcom.sun.management.jmxremote.ssl=false
#      -Djava.rmi.server.hostname=127.0.0.1
      -Dlogback.configurationFile="$dir/logback.xml"
      com.synsere.service.SynsereService
      # synsere service args
      -service_class=${service.class}
      #application args
      ${application.args}
    )
    jsvc "${options[@]}"
}

running()
{
    if [ -f "$pidfile" ]; then
        echo "${project.artifactId} is running"
    else
        echo "${project.artifactId} is not running"
    fi
}

case "$1" in
    start)
        delegate
        running
            ;;
    stop)
        delegate "-stop"
            ;;
    restart)
        if [ -f "$pidfile" ]; then
            echo "Restarting ${project.artifactId}."
            delegate "-stop"
            delegate
            running
        else
            echo "${project.artifactId} not running, will do nothing."
            exit 1
        fi
            ;;
    status)
        running
            ;;
    config)
        echo "project   : ${project.artifactId}, version ${project.version}"
        echo "Java home : $java_home"
        echo "jsvc      : $jsvc"
        echo "config    : $config"
        echo "built     : ${build.timestamp}"
            ;;
    *)
            echo "usage: service.sh {start|stop|restart|status|config}" >&2
            exit 3
            ;;
esac

