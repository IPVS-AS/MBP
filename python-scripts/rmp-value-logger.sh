#!/usr/bin/env sh

source /usr/bin/virtualenvwrapper.sh

workon /etc/rmp/python_env/rmpdiscovery
python /opt/rmp/value-logger.py