#!/usr/bin/env bash
set -x -e

echo "**** PATH ****"
printenv PATH
echo "**** ****"
export PATH=$PATH:/usr/sbin
sudo mkdir -p ~geode/.ssh
sudo chown -R geode:geode ~geode/.ssh


