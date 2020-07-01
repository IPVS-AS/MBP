from requests_oauthlib import OAuth2Session
import os 
import json

# Only for local testing if no https is available
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

# Adapt to production url
token_url = 'http://localhost:8080/MBP/oauth/token'

def get_access_token(client_id, client_secret, authorization_code):
    oauth = OAuth2Session(client_id)
    token = oauth.fetch_token(token_url, code=authorization_code, client_secret=client_secret, method='POST')
    print(token["access_token"])
    return token["access_token"]