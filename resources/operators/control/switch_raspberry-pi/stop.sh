#!/bin/bash
sudo kill -9 $(ps -ef | grep switch_raspberry-pi.py | grep -v grep | awk '{print $2}')