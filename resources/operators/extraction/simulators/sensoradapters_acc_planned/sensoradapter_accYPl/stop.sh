#!/bin/bash
sudo kill -9 $(ps -ef | grep AccYPlSim.jar | grep -v grep | awk '{print $2}')