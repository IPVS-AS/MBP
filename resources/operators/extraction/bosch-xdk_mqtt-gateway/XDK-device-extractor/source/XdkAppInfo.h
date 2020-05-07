/*
* Licensee agrees that the example code provided to Licensee has been developed and released by Bosch solely as an example to be used as a potential reference for application development by Licensee. 
* Fitness and suitability of the example code for any use within application developed by Licensee need to be verified by Licensee on its own authority by taking appropriate state of the art actions and measures (e.g. by means of quality assurance measures).
* Licensee shall be responsible for conducting the development of its applications as well as integration of parts of the example code into such applications, taking into account the state of the art of technology and any statutory regulations and provisions applicable for such applications. Compliance with the functional system requirements and testing there of (including validation of information/data security aspects and functional safety) and release shall be solely incumbent upon Licensee. 
* For the avoidance of doubt, Licensee shall be responsible and fully liable for the applications and any distribution of such applications into the market.
* 
* 
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions are 
* met:
* 
*     (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer. 
* 
*     (2) Redistributions in binary form must reproduce the above copyright
*     notice, this list of conditions and the following disclaimer in
*     the documentation and/or other materials provided with the
*     distribution.  
*     
*     (3)The name of the author may not be used to
*     endorse or promote products derived from this software without
*     specific prior written permission.
* 
*  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR 
*  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
*  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
*  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
*  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
*  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
*  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
*  POSSIBILITY OF SUCH DAMAGE.
*/
/*----------------------------------------------------------------------------*/
/**
 * @file
 * @brief This File represents the Module IDs for the Application C modules
 * and application specific custom error codes.
 *
 */

#ifndef XDK_APPINFO_H_
#define XDK_APPINFO_H_

/* own header files*/
#include "XdkCommonInfo.h"
#include "BCDS_Retcode.h"

/**< Main command processor task priority */
#define TASK_PRIO_MAIN_CMD_PROCESSOR                (UINT32_C(3))
/**< Main command processor task stack size */
#define TASK_STACK_SIZE_MAIN_CMD_PROCESSOR          (UINT32_C(1600))
/**< Main command processor task queue length */
#define TASK_Q_LEN_MAIN_CMD_PROCESSOR               (UINT32_C(10))

/**< Application controller task priority */
#define TASK_PRIO_APP_CONTROLLER                    (UINT32_C(3))
/**< Application controller task stack size */
#define TASK_STACK_SIZE_APP_CONTROLLER              (UINT32_C(1200))

/**
 * @brief BCDS_APP_MODULE_ID for Application C module of XDK
 * @info  usage:
 *      #undef BCDS_APP_MODULE_ID
 *      #define BCDS_APP_MODULE_ID BCDS_APP_MODULE_ID_xxx
 */
enum XDK_App_ModuleID_E
{
    XDK_APP_MODULE_ID_MAIN = XDK_COMMON_ID_OVERFLOW,
    XDK_APP_MODULE_ID_APP_CONTROLLER,

/* Define next module ID here */
};

enum XDK_App_Retcode_E
{
    RETCODE_NODE_IPV4_IS_CORRUPTED = RETCODE_XDK_APP_FIRST_CUSTOM_CODE,
    RETCODE_NODE_WLAN_CONNECTION_IS_LOST,
};

#endif /* XDK_APPINFO_H_ */
