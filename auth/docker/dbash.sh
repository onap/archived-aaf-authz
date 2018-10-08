#!/bin/bash
. ./d.props
${DOCKER:=docker} exec -it aaf_$1 bash
