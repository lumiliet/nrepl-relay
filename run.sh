#!/bin/sh

dir=$(dirname $(readlink -f $0))

(cd "$dir"; java -jar target/relapse-*-standalone.jar >> logs/server.log 2>&1)

