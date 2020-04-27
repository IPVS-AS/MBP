#!/bin/bash
sudo kill -9 $(ps -ef | grep AccYSim.jar | grep -v grep | awk '{print $2}')