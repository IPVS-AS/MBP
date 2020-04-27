#!/bin/bash
sudo kill -9 $(ps -ef | grep PlannedTempSim.jar | grep -v grep | awk '{print $2}')