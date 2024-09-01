# SciCMS Core

Headless open source content management system.

Initially, the system was created as an integration basis for managing data from various sources in industrial enterprises.
SciCMS (short for Scientific CMS) focuses on supporting high-tech industries.
In such industries, the product lifecycle is characterized by a large amount of data with a complex multi-level structure.
Additionally, there are a number of requirements for versioning and multilingual records, as well as the possibility of consolidating data from various sources.
The development of the system was influenced by projects such as [Aras Innovator](https://aras.com) and [Strapi](https://strapi.io).

## Main features of the system

- simple and flexible management of stored data structures;
- wide range of data types;
- GraphQL API;
- support for data versioning;
- multilingual support;
- support for multiple data sources;
- support for relationships between stored Items (one-to-one, many-to-one, one-to-many, many-to-many);
- access control at the record level;
- a mechanism for blocking records from being changed by other users;
- user authentication locally and using the OAuth2 protocol;
- support for storing files both in the local file system and in S3;
- lifecycle hooks of stored Items;
- tools for building analytical reporting (in combination with the client application [SciCMS Client](https://github.com/borisblack/scicms-client) constitute a BI system);
- the system is ready for cloud deployment in several copies with separation of common data and a caching layer.

## Get Started

### Build and run

SciCMS Core is a Kotlin application based on the Spring Boot 3 framework.
To build and run, you need a Java version of at least 21. To run the build enter the command `./gradlew build` from the working directory.
The subsequent launch of the created JAR archive is performed with the command `java -jar <build_path>`.
Before starting, we must set the following environment variables:
- MAIN_DB_URL - standard JDBC connection string (PostgreSQL, Oracle, SQLite and H2 DBMS are currently supported);
- MAIN_DB_USERNAME - database user name;
- MAIN_DB_PASSWORD - database user password;
- REDIS_HOST - Redis server address (default value - `127.0.0.1`);
- REDIS_PORT - Redis port (default value is `6379`).

Examples of connection strings:
- PostgreSQL - `jdbc:postgresql://127.0.0.1:5432/scicms`;
- Oracle - `jdbc:oracle:thin:@//127.0.0.1:1521/xepdb1`;
- SQLite - `jdbc:sqlite:scicms.db?date_class=text`;
- H2 - `jdbc:h2:file:./scicms.h2`.

If we do not have configured DBMS and Redis instances, we can use the file [docker-compose-infra.yml](docker/docker-compose-infra.yml) to launch them with [Docker](http://docker.io):
```shell
cd ./docker
docker-compose -f docker-compose-infra.yml up -d --build
```

In this case, when running on the local computer, the environment variables should have the following values: MAIN_DB_URL = `jdbc:postgresql://127.0.0.1:5432/scicms`, MAIN_DB_USERNAME = `scicms`, MAIN_DB_PASSWORD = `scicms`.
If the built-in database (SQLite or H2) is used, then on the first launch a file is created at the path specified in the connection string.
In future versions of SciCMS Core, in order to simplify local launch, it is planned to add the ability for the application to work with a cache in its own memory.

### Authentication for interacting with the API
After the system is launched on the local computer, the server communicates at `http://127.0.0.1:8079` (the port number is specified by the `server.port` parameter in the file [application.yml](src/main/resources/application.yml) ).
To interact with the API, you must authenticate and obtain a token. Authentication can be local or using the Oauth2 protocol.

With local authentication, obtaining a token is done through a `POST /api/auth/local` request with the body:
```json
{
  "username": "root",
  "password": "master"
}
```

The user account `root` with the password `master` is created the first time the system starts with full rights.
The response body contains the JWT token and other information about the user:
```json
{
  "jwt": "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJzY2lzb2x1dGlvbnNKV1QiLCJzdWIiOiJyb290IiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9BRE1J...",
  "user": {
    "id": "0c924266-3c61-4362-81d7-9d69403fbe32",
    "username": "root",
    "roles": [
      "ROLE_ADMIN"
    ],
    "authType": "LOCAL"
  },
  "expirationIntervalMillis": 86400000
}
```

**In all subsequent requests** the resulting token must be present in the `Authorization` header: `Authorization: Bearer <jwt>`.

The OpenAPI specification for all REST methods is available through the running application on the page `/swagger-ui.html`.

Authentication using the OAuth2 protocol is discussed in the [Security](/docs/security.md) section.
The client application [SciCMS Client](https://github.com/borisblack/scicms-client) implements both authentication methods.

### Data model

The central concept in SciCMS Core is **Item**.
In the object-oriented programming analogy, an Item is a class (a description of fields and methods), and a record for a given Item is an instance of a class (specific data).
Metadata elements are also predefined Items.
The easiest way is to create a declarative Item definition in a YAML (or JSON) file (for example, `book-item.yml`).
This definition is very similar in purpose and structure to manifests in the [Kubernetes](https://kubernetes.io/) ecosystem.
An example of defining an Item in SciCMS Core:

```yaml
# book
coreVersion: v1
kind: Item
includeTemplates:
  - default
metadata:
  name: book
  displayName: Book
  pluralName: books
  displayPluralName: Books
  dataSource: main
  tableName: books
  description: Books
  performDdl: true
spec:
  attributes:
    name:
      type: string
      columnName: name
      displayName: Name
      description: Name
      required: true
      unique: true
      length: 50
    rating:
      type: int
      columnName: rating
      displayName: Rating
      description: Rating
      minRange: 0
      maxRange: 10
```

In this example, an Item is created with the name `book` and two attributes `name` and `rating`, respectively of type `string` and `int`.
For more information about Item attributes, see the [Data Model](/docs/data_model.md) section.

Once the Item definition file has been created, it must be placed in the [schema](/src/main/resources/schema) directory (or any of its subdirectories) and the application restarted.
At startup, the system will scan all changes in the directory and apply them to the current data schema.
As a result, the Item and corresponding objects will be created in the database.
Similarly, if a file is changed, the subsequent launch of the application will also entail the necessary changes.
In addition to YAML, an Item definition can be created in JSON format ([example](/src/main/resources/schema/example-item.json)).
For each Item, the system generates the set of operations required for it in accordance with its name.
Below are examples of operations for the `book` Item. The first two operations in the GraphQL schema are of type `query`, the rest are of type `mutation`.
- `books` - returns a list of records;
- `book` - returns a specific record;
- `createBook` - creating a record;
- `createBookVersion` - creating a new version of a record for Items with the `versioned` flag;
- `createBookLocalization` - creating a new record localization for Items with the `localized` flag;
- `updateBook` - changing a record;
- `deleteBook` - deleting a record;
- `purgeBook` - deleting all versions of a record;
- `lockBook` - locking a record for an Item without the `notLockable` flag;
- `unlockBook` - unlocking a record for an Item without the `notLockable` flag;
- `promoteBook` - moving a record to the next stage of the lifecycle (see section [Item lifecycle](/docs/lifecycle.md)).

In addition to the listed methods, an Item can contain arbitrary custom methods; for more information about them, see the section [Item lifecycle](/docs/lifecycle.md).

Let's look at examples of basic operations on Item records.
When the application is running, we can use the built-in version of Graph*i*QL at `/graphiql` (before executing the request, we need to add the `Authorization` header with the received token in the headers field).
We can also use any UI tool that supports GraphQL ([Postman](https://www.postman.com), [Insomnia](https://insomnia.rest), etc.).

### Creating

Example GraphQL query:
```
mutation {
  createBook(
    data: {
      name: "Alice's Adventures in Wonderland"
      rating: 8
    }
  ) {
    data {
      id
      name
      rating
      permission {
        data {
          name
        }
      }
    }
  }
}
```

The response will contain the data of the created record:

```json
{
  "data": {
    "createBook": {
      "data": {
        "id": "d160cdfd-e548-412f-9249-d27de0274499",
        "name": "Alice's Adventures in Wonderland",
        "rating": 9,
        "permission": {
          "data": {
            "name": "Default Permission"
          }
        }
      }
    }
  }
}
```

### Updating

Example GraphQL query:
```
mutation {
  updateBook(
    id: "d160cdfd-e548-412f-9249-d27de0274499"
    data: {
        rating: 9
    }
  ) {
    data {
      id
      name
      rating
    }
  }
}
```

### Deleting

```
mutation {
  deleteBook(
    id: "d160cdfd-e548-412f-9249-d27de0274499"
    deletingStrategy: NO_ACTION
  ) {
    data {
      id
      name
      rating
    }
  }
}
```

### Retrieving Item data

```
query {
  book(
      id: "d160cdfd-e548-412f-9249-d27de0274499"
  ) {
    data {
      id
      name
      rating
    }
  }
}
```

### Search and filtering

Example GraphQL query:
```
query {	
  books(
    filters: {
      name: {
        containsi: "alice"
        notContainsi: "jane"
      }
      rating: {
        gte: 8
      }
      or: {
        updatedBy: {
          username: {
            eq: "root"
          }
        }
      }
    }
    sort: ["name:asc"]
    pagination: {
      page: 1
      pageSize: 20
    }
  ) {
    data {
      id
      name
      rating
      permission {
        data {
          name
        }
      }
    }
    meta {
      pagination {
        page
        pageSize
        pageCount
        total
      }
    }
  }
}
```

Sample response:
```json
{
  "data": {
    "books": {
      "data": [
        {
          "id": "d160cdfd-e548-412f-9249-d27de0274499",
          "name": "Alice's Adventures in Wonderland",
          "rating": 9,
          "permission": {
            "data": {
              "name": "Default Permission"
            }
          }
        }
      ],
      "meta": {
        "pagination": {
          "page": 1,
          "pageCount": 1,
          "total": 1
        }
      }
    }
  }
}
```

For more information about filtering and paging parameters, as well as other operations, see the [Data model](/docs/data_model.md) section.

The entire described API is also used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client), which provides a convenient user interface for managing Items and access, as well as many other functions.

## Additional resources

[Data model](/docs/data_model.md)

[Items lifecycle](/docs/lifecycle.md)

[Security](/docs/security.md)

[Working with files](/docs/media.md)

[Analytics tools](/docs/analytics.md)