#!/usr/bin/env bash

export AWS_PROFILE="geode-benchmarks"
packer build packer.json
