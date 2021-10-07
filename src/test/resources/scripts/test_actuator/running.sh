#!/bin/bash
tmux_session=$(tmux list-sessions | grep scriptSession | grep -v grep );
if [[ $tmux_session != "" ]]; then
   echo "true"; #is running
else
   echo "false"; # is not running
fi
