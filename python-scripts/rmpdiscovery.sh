#!/usr/bin/env sh

export WORKON_HOME=/etc/rmpdiscovery/python_env
source /usr/bin/virtualenvwrapper.sh

workon rmpdiscovery
python /opt/rmpdiscovery/rmpdiscovery.py --lan --bt