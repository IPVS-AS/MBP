from requests_oauthlib import OAuth2Session
from requests.auth import HTTPBasicAuth
import os 
import json

# Only for local testing if no https is available
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

def get_access_token(token_url, client_id, client_secret, authorization_code):
    auth = HTTPBasicAuth(client_id, client_secret)
    oauth = OAuth2Session(client_id)
    token = oauth.fetch_token(token_url=token_url, code=authorization_code, method='POST', auth=auth)
    return token["access_token"], token["refresh_token"]

def get_access_token_with_refresh_token(token_url, client_id, client_secret, refresh_token):
    auth = HTTPBasicAuth(client_id, client_secret)
    client = OAuth2Session(client_id)
    token = client.refresh_token(token_url, refresh_token, auth=auth)
    print(token["access_token"])
    print(token["refresh_token"])
    return token["access_token"], token["refresh_token"]