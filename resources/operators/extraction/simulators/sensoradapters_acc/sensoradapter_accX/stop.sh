#!/bin/bash
sudo kill -9 $(ps -ef | grep AccXSim.jar | grep -v grep | awk '{print $2}')