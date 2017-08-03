#!/bin/bash

set -e

host="$1"
port="$2"
shift
shift
cmd="$@"

until echo > /dev/tcp/${host}/${port} ; do
  >&2 echo "${host}:${port} is unavailable - sleeping"
  sleep 1
done

>&2 echo "${host}:${port} is up - executing command"
exec $cmd
