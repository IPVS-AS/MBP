#!/bin/bash
rsyslogd
ssh-keygen -P "" -t dsa -f /etc/ssh/ssh_host_dsa_key
ssh-keygen -P "" -t rsa -f /etc/ssh/ssh_host_rsa_key
ssh-keygen -P "" -t ed25519 -f /etc/ssh/ssh_host_ed25519_key
/usr/sbin/sshd -D