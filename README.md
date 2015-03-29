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

Under `mongodb` you can add several element to configure several mongodb servers / databases.

## Configuration using `cfg` files

You can also configure the extension using `cfg` files. Each `cfg` file creates an instance of the MongoDB Client. 
Create a file named `org.wisdom.mongodb.MongoDBClient-YOUR_NAME.cfg` into `src/main/configuration`.
 
```
hostname: localhost
port: 12345
dbname: kitten
``` 

## Using MongoDB

For each instantiate client, a `com.mongodb.DB` is exposed. This service is only exposed if the database is available
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