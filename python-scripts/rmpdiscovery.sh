#!/usr/bin/env sh

export WORKON_HOME=/etc/rmpdiscovery/python_env
source /usr/bin/virtualenvwrapper.sh

workon /etc/rmpdiscvovery/python_env/rmpdiscovery
python /opt/rmpdiscovery/rmpdiscovery.py --lan --bt