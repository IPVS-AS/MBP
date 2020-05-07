#!/bin/bash
sudo kill -9 $(ps -ef | grep loudspeaker_computer.py | grep -v grep | awk '{print $2}')