FROM ubuntu:21.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y sudo git openssh-server openssh-client nano vim python3 python3-pip python3-venv tmux htop rsyslog iproute2 && \
    pip3 install paho-mqtt && \
    apt-get clean -y && \
    useradd -m -U -s /bin/bash mbp  && \
    groupadd wheel && \
    usermod -aG wheel mbp && \
    rm -v /etc/ssh/ssh_host_rsa_key /etc/ssh/ssh_host_ecdsa_key /etc/ssh/ssh_host_ed25519_key && \
    mkdir -p /run/sshd && \
    echo "password\npassword" | passwd mbp && \
    echo "password\npassword" | passwd root
COPY sudoers /etc/sudoers
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh
ENTRYPOINT /entrypoint.sh
EXPOSE 22