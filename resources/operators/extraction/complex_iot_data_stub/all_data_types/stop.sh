#!/bin/bash
sudo kill -9 $(ps -ef | grep test_all_mbp_data_types.py | grep -v grep | awk '{print $2}')