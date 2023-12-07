# Prerequisite
After cloning the repo, in order to get the app up and running: create a file local.properties.
In this file specify:

* sdk.dir=\<path/to/androidsdk\> [help](https://stackoverflow.com/questions/25176594/android-sdk-location)
* WEBAPPENDPOINT=10.0.2.2:8080 localhost won't work because you are in an emulator, use 10.0.2.2

For the backend code:

```
cd backend
npm install
touch .env
```
Paste the api key with key "API_KEY="

to run python backend:


```
uvicorn main:app --reload
```