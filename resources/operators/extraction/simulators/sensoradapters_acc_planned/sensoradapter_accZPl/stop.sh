#!/bin/bash
sudo kill -9 $(ps -ef | grep AccZPlSim.jar | grep -v grep | awk '{print $2}')