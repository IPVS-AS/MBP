#!/bin/bash
sudo kill -9 $(ps -ef | grep actuator_stub.py | grep -v grep | awk '{print $2}')