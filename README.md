# Wisdom MongoDB

This project is an extension for Wisdom Framework, to allow the integration of MongoDB database.

## Installation

Add the following dependency to your `pom.xml` file:

````
<dependency>
  <groupId>org.wisdom-framework</groupId>
  <artifactId>wisdom-mongodb</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
````

## Configuration using the `application.conf` file

In the `src/main/configuration/application.conf` file add the following snippet:

````
###
# MongoDB Configuration
###
mongodb {
  test { # Name used to identify the data source. If `datasources` is not set, it will be used
    hostname: localhost
    port: 12345
    dbname: kitten
  }
}
````

Under `mongodb` you can add several elements to configure several mongodb servers / databases.

## Configuration using `cfg` files

You can also configure the extension using `cfg` files. Each `cfg` file creates an instance of the MongoDB Client. 
Create a file named `org.wisdom.mongodb.MongoDBClient-YOUR_NAME.cfg` into `src/main/configuration`.
 
```
hostname: localhost
port: 12345
dbname: kitten
``` 

## Using MongoDB

For each instantiated client, a `com.mongodb.DB` is exposed. This service is only exposed if the database is available
. A heartbeat is setup to track the availability. If the service is exposed, it means that the connection has been 
established and should work.
 
The service is exposed with the following properties that let you select the right one:

* `host`: the host
* `port`: the port
* `name`: the name of the database
* `datasources`: array of string
 
Here are some examples:
 
```
@Requires(filter="(host=localhost)") // database host
DB localhost;

@Requires(filter="(name=test)") // database name
DB localhost;

@Requires(filter="(datasources=cat)") // datasource names
DB localhost;
``` 

## Configuration Property

TODO
These are the different Mongo Client options you can configure:


###description

Sets the description of the Mongo Client takes a string.

###connectionsPerHost

Sets the maximum number of connections per host, takes an integer, must be greater than 1, default is 2.

###threadsAllowedToBlockForConnectionMultiplier

Sets the multiplier for number of threads allowed to block waiting for a connection. Takes an integer that must be greater than 0.


###maxWaitTime
Sets the maximum time that a thread will block waiting for a connection. Takes an integer that cannot be less than 0.


###connectTimeout
Sets the connection timeout. Takes an integer, that cannot be less than 0.

###autoConnectRetry
Sets whether auto connect retry is enabled, takes a boolean true or false.

###maxAutoConnectRetryTime
Sets the maximum auto connect retry time, takes an integer.

###writeConcern
Sets the write concern.
individual params:
WriteConcern(int w, int wtimeout, boolean fsync, boolean j)

###socketTimeout
Sets the socket timeout.

###socketKeepAlive
Sets whether socket keep alive is enabled.

###socketFactory
Sets the socket factory.


