#Use debian:stable-slim as a builder and then copy everything.
FROM debian:stable-slim as builder

ARG is_secure="false"

RUN if [ -z "$is_secure" ] ; then echo is_secure argument not provided ; else echo is_secure: $is_secure ; fi

#Set mosquitto and plugin versions.
#Change them for your needs.
ENV MOSQUITTO_VERSION=1.6.9
ENV PLUGIN_VERSION=0.7.0

WORKDIR /app

#Get mosquitto build dependencies.
RUN apt-get update && apt-get install -y libwebsockets16 libwebsockets-dev libc-ares2 libc-ares-dev openssl uuid uuid-dev wget build-essential git libc-ares2 openssl uuid golang
RUN mkdir -p mosquitto/auth mosquitto/conf.d

RUN wget http://mosquitto.org/files/source/mosquitto-${MOSQUITTO_VERSION}.tar.gz
RUN tar xzvf mosquitto-${MOSQUITTO_VERSION}.tar.gz && rm mosquitto-${MOSQUITTO_VERSION}.tar.gz 

#Build mosquitto.
RUN cd mosquitto-${MOSQUITTO_VERSION} && make WITH_WEBSOCKETS=yes && make install && cd ..

#Get the plugin.
RUN wget https://github.com/iegomez/mosquitto-go-auth/archive/${PLUGIN_VERSION}.tar.gz \
    && tar xvf *.tar.gz --strip-components=1 \
    ; rm -rf go*.tar.gz

#Build the plugin.
RUN export PATH=$PATH:/usr/local/go/bin && export CGO_CFLAGS="-I/usr/local/include -fPIC" \
    && export CGO_LDFLAGS="-shared" && pwd && ls -la; make

#Start from a new image.
FROM debian:stable-slim

#Get mosquitto dependencies.
RUN apt-get update && apt-get install -y libwebsockets16 libc-ares2 openssl uuid

#Setup mosquitto env.
RUN mkdir -p /var/lib/mosquitto /var/log/mosquitto 
RUN groupadd mosquitto \
    && useradd -s /sbin/nologin mosquitto -g mosquitto -d /var/lib/mosquitto \
    && chown -R mosquitto:mosquitto /var/log/mosquitto/ \
    && chown -R mosquitto:mosquitto /var/lib/mosquitto/

#Copy confs, plugin so and mosquitto binary.
COPY --from=builder /app/mosquitto/ /mosquitto/
COPY --from=builder /app/go-auth.so /tmp/go-auth.so
COPY --from=builder /usr/local/sbin/mosquitto /usr/sbin/mosquitto

#Uncomment to copy your custom confs (change accordingly) directly when building the image.
#Leave commented if you want to mount a volume for these (see docker-compose.yml).

COPY mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY mosquitto-go-auth.conf /tmp/go-auth.conf
RUN mkdir -p /etc/mosquitto/conf.d; if [ "$is_secure" = "true" ] ; then mv /tmp/go-auth.so /mosquitto/go-auth.so; \
    cp /tmp/go-auth.conf /etc/mosquitto/conf.d/go-auth.conf ; fi

#COPY conf/auth/acls /etc/mosquitto/auth/acls
#COPY conf/auth/passwords /etc/mosquitto/auth/passwords

#Expose tcp and websocket ports as defined at mosquitto.conf (change accordingly).
EXPOSE 1883 1884
VOLUME /var/lib/mosquitto
USER mosquitto

ENTRYPOINT ["sh", "-c", "/usr/sbin/mosquitto -c /etc/mosquitto/mosquitto.conf" ]