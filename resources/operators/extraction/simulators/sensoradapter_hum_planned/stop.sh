#!/bin/bash
sudo kill -9 $(ps -ef | grep HumSimPlanned.jar | grep -v grep | awk '{print $2}')