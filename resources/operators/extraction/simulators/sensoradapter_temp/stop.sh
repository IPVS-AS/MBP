#!/bin/bash
sudo kill -9 $(ps -ef | grep TempSim.jar | grep -v grep | awk '{print $2}')