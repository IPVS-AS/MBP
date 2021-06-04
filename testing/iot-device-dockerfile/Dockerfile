FROM ubuntu:21.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y sudo git openssh-server openssh-client nano vim python3 python3-pip python3-venv tmux htop 
RUN useradd -m -U -s /bin/bash mbp
RUN groupadd wheel
RUN usermod -aG wheel mbp
COPY sudoers /etc/sudoers
RUN rm -v /etc/ssh/ssh_host_rsa_key /etc/ssh/ssh_host_ecdsa_key /etc/ssh/ssh_host_ed25519_key
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh
RUN mkdir -p /run/sshd
RUN echo "password\npassword" | passwd mbp
RUN echo "password\npassword" | passwd root
ENTRYPOINT /entrypoint.sh