#!/bin/bash
sudo kill -9 $(ps -ef | grep HumSim.jar | grep -v grep | awk '{print $2}')