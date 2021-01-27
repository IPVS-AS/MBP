#!/usr/bin/env python
# -*- coding: utf-8 -*-
import json
import sys
import time

import jsonpath_ng.ext as jsp
import requests

from mbp_client import MBPclient

# Dict of HTTP methods
HTTP_METHODS = {
    'get': requests.get,
    'post': requests.post,
    'put': requests.put,
    'patch': requests.patch,
    'delete': requests.delete,
    'head': requests.head,
    'options': requests.options
}


def main(argv):
    # Extract relevant parameters from command line arg
    params = __extract_parameters(argv[1])

    # instantiate the MBP client
    mbp = MBPclient()

    # initialize the MBP client
    mbp.connect()

    # Init JsonPath exp for extracting values from responses
    jsonpath_exp = jsp.parse(params['jsp_exp'])

    try:
        # This loop ensures your code runs continuously
        while True:
            #############################
            # Get matching request function
            request_func = HTTP_METHODS[params['method']] if params['method'] in HTTP_METHODS else requests.get

            # Perform HTTP request
            request_response = request_func(params['url'])

            # Check status code for 2xx
            if divmod(request_response.status_code, 100)[0] != 2:
                print("Error: Received stats code {}.".format(request_response.status_code))
                extracted_value = -1
            else:
                # Parse JSON response
                response_json = json.loads(request_response.content.decode("utf-8"))

                # Extract value using the JsonPath expression
                jsp_match = jsonpath_exp.find(response_json)
                extracted_value = jsp_match[0].value if len(jsp_match) > 0 else -1
            #############################

            # send data to the MBP
            mbp.send_data(extracted_value)

            # waits a time interval before sending new data
            time.sleep(params['interval'])
    except:
        error = sys.exc_info()
        print('Error:', str(error))

    # terminate the MBP client
    mbp.finalize()


def __extract_parameters(params: str):
    # Parse parameter values
    params_json = json.loads(params)

    # Set default values for parameters
    param_values = {
        'url': "",
        'method': "get",
        'jsp_exp': "$.`len`",
        'interval': 60
    }

    # Iterate over list of extracted parameters
    for param in params_json:
        if not ('name' in param and 'value' in param):
            continue
        elif param['name'].lower() not in param_values:
            continue

        # Update parameter value
        param_values[param['name'].lower()] = param['value']

    # Return updated parameter values
    return param_values


if __name__ == "__main__":
    main(sys.argv[1:])
