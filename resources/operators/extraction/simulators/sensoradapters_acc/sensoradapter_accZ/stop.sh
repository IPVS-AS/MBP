#!/bin/bash
sudo kill -9 $(ps -ef | grep AccZSim.jar | grep -v grep | awk '{print $2}')