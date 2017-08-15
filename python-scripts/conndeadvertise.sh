#!/usr/bin/env sh

export WORKON_HOME=/home/rosso/python_env
source /usr/bin/virtualenvwrapper.sh

workon connde
python /opt/conndeadvertise/conndeadvertise.py --lan