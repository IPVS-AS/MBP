# This makefile triggers the targets in the application.mk

# The default value "../../.." assumes that this makefile is placed in the 
# folder xdk110/Apps/<App Folder> where the BCDS_BASE_DIR is the parent of 
# the xdk110 folder.
BCDS_BASE_DIR ?= ../../..

# Macro to define Start-up method. change this macro to "CUSTOM_STARTUP" to have custom start-up.
export BCDS_SYSTEM_STARTUP_METHOD = DEFAULT_STARTUP
export BCDS_APP_NAME = XDK-device-extractor
export BCDS_APP_DIR = $(CURDIR)
export BCDS_APP_SOURCE_DIR = $(BCDS_APP_DIR)/source
export BCDS_FREERTOS_INCLUDE_AWS=1

# Macro for server cetificate which is used for secure communication.
# User can provide custom server certificate as well.
# By default the test.mosquito.org MQTT open source broker certificate is configured.
export XDK_APP_CERTIFICATE_NAME = Mosquitto
#Please refer BCDS_CFLAGS_COMMON variable in application.mk file
#and if any addition flags required then add that flags only in the below macro 
#export BCDS_CFLAGS_COMMON =

#Below settings are done for optimized build.Unused common code is disabled to reduce the build time
export XDK_FEATURE_SET='SELECT'

#Enabled feature macros for common code
export XDK_CONNECTIVITY_MQTT=1
export XDK_CONNECTIVITY_WLAN=1

export XDK_SENSOR_SENSOR=1

#end of settings related to optimized build

# This variable should fully specify the build configuration of the Serval
# Stack library with regards the enabled and disabled features for the HTTPS Using TLS.
export SERVAL_TLS_MBEDTLS=1
export SERVAL_ENABLE_TLS_CLIENT=1
export SERVAL_ENABLE_TLS_ECC=1
export SERVAL_ENABLE_TLS_PSK=0
export SERVAL_MAX_NUM_MESSAGES=8
export SERVAL_MAX_SIZE_APP_PACKET=900
export SERVAL_ENABLE_TLS=1

export XDK_MBEDTLS_PARSE_INFO=0

#List all the application header file under variable BCDS_XDK_INCLUDES
export BCDS_XDK_INCLUDES = \

#List all the application source file under variable BCDS_XDK_APP_SOURCE_FILES in a similar pattern as below
export BCDS_XDK_APP_SOURCE_FILES = \
	$(wildcard $(BCDS_APP_SOURCE_DIR)/*.c)

.PHONY: clean debug release flash_debug_bin flash_release_bin

clean: 
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk clean

debug: 
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk debug
	
release: 
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk release
	
clean_Libraries:
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk clean_libraries
	
flash_debug_bin: 
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk flash_debug_bin
	
flash_release_bin: 
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk flash_release_bin
	
cdt:
	$(MAKE) -C $(BCDS_BASE_DIR)/xdk110/Common -f application.mk cdt	
	
	
