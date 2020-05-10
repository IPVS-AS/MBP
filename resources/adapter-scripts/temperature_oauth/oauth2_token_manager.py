from requests_oauthlib import OAuth2Session
from requests.auth import HTTPBasicAuth
import os 
import json

# Only for local testing if no https is available
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

# Adapt to production url
token_url = 'http://193.196.54.147:8080/MBP/oauth/token'

def get_access_token(client_id, client_secret, authorization_code):
    auth = HTTPBasicAuth(client_id, client_secret)
    oauth = OAuth2Session(client_id)
    token = oauth.fetch_token(token_url=token_url, code=authorization_code, method='POST', auth=auth)
    return token["access_token"], token["refresh_token"]

def get_access_token_with_refresh_token(client_id, client_secret, refresh_token):
    auth = HTTPBasicAuth(client_id, client_secret)
    client = OAuth2Session(client_id)
    token = client.refresh_token(token_url, refresh_token, auth=auth)
    return token["access_token"], token["refresh_token"]