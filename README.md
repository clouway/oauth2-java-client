# OAuth2 java client
A client library which uses client secrets and JWT to generate access tokens

###Google HTTP client adapter

####1. with client secrets 
```java
OАuthClientConfig config = OАuthClientConfig
       .setClient("::client::")
       .setSecret("::secret::")
       .setScopes(setOf("scope1", "scope2"))
       // here you should create your own CredentialsStore
       // implementation 
       .setCredentialsStore(credentialsStore)
       .build()
             
HttpRequestFactory factory = NetHttpTransport().createRequestFactory(
         TokenHeaderInterceptor.newGoogleInterceptor(config))
 
 // do HTTP call
 ```
 
 NOTE: your client should have redirect url : 'http://localhost:8089/oauth2callback'
 
 NOTE: if this code is invoked by gradle task(groovy) you task should contain this line:
 'standardInput = System.in' or Scanner object will throw exception.
