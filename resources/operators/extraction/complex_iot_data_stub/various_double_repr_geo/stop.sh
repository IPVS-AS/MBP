#!/bin/bash
sudo kill -9 $(ps -ef | grep nested_double_test.py | grep -v grep | awk '{print $2}')