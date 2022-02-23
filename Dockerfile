FROM openjdk:11-jre
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y sudo bash curl
RUN mkdir -p /usr/src/app
COPY target/MBP.jar /usr/src/app
WORKDIR /usr/src/app
EXPOSE 8080
COPY /docker_start/entrypoint.sh ./entrypoint.sh
RUN ["chmod", "+x" , "./entrypoint.sh"]
COPY /docker_start/create_device.sh ./create_device.sh
RUN ["chmod", "+x" , "./create_device.sh"]
COPY /docker_start/start_mbp.sh ./start_mbp.sh
RUN ["chmod", "+x" , "./start_mbp.sh"]
ENTRYPOINT ["./entrypoint.sh"]