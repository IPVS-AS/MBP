#!/bin/bash
sudo kill -9 $(ps -ef | grep rerun.py | grep -v grep | awk '{print $2}')