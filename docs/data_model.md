# Data model

The SciCMS Core architecture involves storing metadata (all information necessary for the system to operate) in the primary relational database called `main`.
To improve performance and provide scalability in a cloud environment, metadata is cached via [Redis](https://redis.io).

Other data can be stored either in the main database or in any other database.
The system allows you to connect unlimited number of databases dynamically at runtime.
In this case, actual connections are opened only at the moment accessing the database without wasting resources on idle connections.
When idle for a long time (configured by the `scicms-core.data.datasource-cache-expiration-minutes` parameter), connections are closed, freeing up system resources.
PostgreSQL and Oracle DBMS are currently supported.
In future versions of SciCMS Core, in order to simplify local launch, it is planned to add the ability to work with the application with a built-in SQLite database and a cache in its own memory.
Also, if necessary, support for other types of DBMS will be added.
The process of creating additional data sources will be discussed later in the current section.

## Items
The central concept in SciCMS Core is **Item**.
In the object-oriented programming analogy, an Item is a class (a description of fields and methods), and a record for a given Item is an instance of a class (specific data).
Metadata elements are also predefined Item. An Item can be created in three ways.

**The first way** is to create a declarative Item definition in a YAML (or JSON) file (for example, `book-item.yml`).
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

A complete description of all fields is in the file [model.schema.json](/src/main/resources/schema/model.schema.json).
The example above describes an Item called `book`, which includes the following fields:
- `coreVersion` - SciCMS Core API version;
- `kind` - Item type (currently supported types are `Item` and `ItemTemplate`);
- `includeTemplates` - included templates (templates contain a set of predefined attributes);
- `metadata` - block with Item metadata;
- `metadata.name` - Item name (in Latin without spaces, used when generating GraphQL API methods);
- `metadata.displayName` - display name of the Item;
- `metadata.pluralName` - plural Item name (in Latin without spaces, used when generating GraphQL API methods);
- `metadata.displayPluralName` - plural Item display name;
- `metadata.dataSource` - name of the data source;
- `metadata.tableName` - name of the table in the database; not all Items correspond to tables in the database; there are read-only Items that, instead of `tableName`, have the `readOnly` = `true` flag and the `query` property with an arbitrary SQL query to fetch data;
- `metadata.description` - Item description (used when generating documentation for types in the GraphQL API);
- `metadata.performDdl` - flag for performing DDL operations in the database when creating/changing an Item (CREATE/ALTER TABLE);
- `spec` - block with the specification of attributes and indexes of the Item;
- `spec.attributes` - Item attributes.

Each Item has an optional parameter `metadata.cacheTtl` - cache lifetime in minutes.
If not specified, the default value of 10 minutes is assumed (the `scicms-core.data.item-query-result-entry-ttl-minutes` parameter in the [application.yml](/src/main/resources/application.yml)).
If the cache value is less than or equal to 0, then Item records are not cached.
The cache works only for read operations. When an Item is changed/deleted, the cache is updated.
If the `scicms-core.schema.clear-cache-on-seed` parameter in the settings is `true` (the default value), then the application resets the cache every time it is restarted.
The cache is also reset when performing operations on system Items (whose `metadata.core` field is `true`), such as `item`, `user`, `group`, `permission`, etc.
By default, the `metadata.core` field is `false` for all created Items.

In the Item definition, each attribute has a unique name (also entered in Latin letters and without spaces) and a specification.
An attribute specification can include a different set of fields depending on the attribute type. Some fields are required, some are not.
Here are some from fields:
- `spec.attributes.<attr_name>.type` - attribute type (see below);
- `spec.attributes.<attr_name>.columnName` - name of the column in the database;
- `spec.attributes.<attr_name>.displayName` - display name of the attribute;
- `spec.attributes.<attr_name>.description` - attribute description;
- `spec.attributes.<attr_name>.required` - attribute mandatory flag;
- `spec.attributes.<attr_name>.unique` - flag of attribute uniqueness within the database table;
- `spec.attributes.<attr_name>.length` - string length (for type `string`);
- `spec.attributes.<attr_name>.minRange` - minimum value (for numeric types);
- `spec.attributes.<attr_name>.maxRange` - maximum value (for numeric types);

### Attribute types

The system supports the following types of attributes:
- `uuid` - [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier) format string;
- `string` - fixed-length string; the maximum length is determined by the `length` attribute field; for a string attribute, the `pattern` parameter can also be specified - a regular expression that the string must match;
- `text` - string of unlimited length;
- `enum` - string enumeration; this attribute requires an `enumSet` field with a list of valid values;
- `sequence` - the attribute value will be taken from the sequence; a sequence is a separate Item and includes attributes such as starting value, prefix, suffix, step, padding, padding length; for an attribute with type `sequence` the parameter `seqName` must be specified, which refers to the name of an existing sequence;
- `email` - email address string;
- `password` - password string; in addition, the `encode` field can be specified - to encode the string when saving to the database;
- `int` - integer type, additional fields `minRange` and `maxRange` can be specified;
- `long` - long integer type, additional fields `minRange` and `maxRange` can be specified;
- `float` - numeric type with a floating point, additionally the `minRange` and `maxRange` fields can be specified;
- `double` - numeric type with double-precision floating point, additional fields `minRange` and `maxRange` can be specified;
- `decimal` - type for a decimal number, additionally the fields `minRange`, `maxRange`, `precision` and `scale` can be specified;
- `date` - type for storing dates;
- `time` - type for storing time;
- `datetime` - type for storing date and time (it is assumed that information about the time zone is stored in the field, if this mechanism is supported by the DBMS);
- `timestamp` - type for storing date and time;
- `bool` - logical type;
- `array` - an array (list) of values, stored in the database in a text field as a JSON array;
- `json` - JSON object, stored in a text field in the database;
- `media` - file. The column stores an identifier that references the `media` Item; for more details, see the section [Working with files](media.md);
- `relation` - relationship with another Item (see below).

### Relationships

There are four types of relations: `oneToOne`, `manyToOne`, `oneToMany` and `manyToMany`. This type is specified in the `relType` field of the attribute.
The second required parameter of the `relation` type attribute is `target` - the name of the Item with which the relation is made.

#### onToOne relationship

This type of relationship implies that there is a single record in another table that is referenced by the current Item's record (the attribute column with type `relation` of the current Item stores the record identifier of the second Item).
In this case, the current Item is the "owner" of the relationship. The Items that make up the `oneToOne` relations can belong to different data sources.
In the example below, the `user` Item owns a relation to the `userInfo` Item through an attribute of the same name (and its own `user_info_id` column):
```yaml
# user
metadata:
  name: user
spec:
  attributes:
    userInfo:
      type: relation
      relType: oneToOne
      target: userInfo
      columnName: user_info_id
```

The `userInfo` attribute refers to the attribute of the second Item, which is specified in its `metadata.idAttribute` parameter (default is `id`).
This behavior can be overridden by adding a `referencedBy` field to the relation attribute.
For example, both the `user` and `userInfo` Items have a string column `username` and need to make up a relation through it.
Then their specifications will look like this:
```yaml
# user
metadata:
  name: user
spec:
  attributes:
    userInfo:
      type: relation
      relType: oneToOne
      target: userInfo
      columnName: username
      referencedBy: username
```

```yaml
# userInfo
metadata:
  name: userInfo
spec:
  attributes:
    username:
      type: string
      columnName: username
      unique: true
      length: 50
```

The `onToOne` relationship can be bidirectional.
Then the second Item also contains a virtual relation to the current one (without a physical table column, since it is not the "owner" of the relationship) with an additional `mappedBy` parameter.
This parameter contains the name of the owner Item attribute by which the relation is built:
```yaml
# userInfo
metadata:
  name: userInfo
spec:
  attributes:
    user:
      type: relation
      relType: oneToOne
      target: user
      mappedBy: userInfo
```

In bidirectional relationship, the owning Item must also contain an additional parameter (called `inversedBy`) on its side.
Similar to `mappedBy`, this parameter contains the name of the attribute of the opposite Item by which the relation is built:
```yaml
# user
metadata:
  name: user
spec:
  attributes:
    userInfo:
      type: relation
      relType: oneToOne
      target: userInfo
      inversedBy: user
```

#### manyToOne relationship

A `manyToOne` relationship implies that there is a record in another table that is referenced by the current Item's records.
The `manyToOne` relation is always "owning", i.e. stores the record identifier of the second Item in its attribute column.
The Items that make up the `manyToOne` and `oneToMany` relations may belong to different data sources. Definition of `manyToOne` relation:
```yaml
# book
metadata:
  name: book
spec:
  attributes:
    userInfo:
      type: relation
      relType: manyToOne
      target: category
      columnName: category_id
```

Also, similar to `oneToOne`, you can override the identifier of the opposite Item by adding the `referencedBy` field to the relation attribute.

#### oneToMany relationship

The `manyToOne` relationship can be bidirectional.
Then the second Item will contain a virtual relation of type `oneToMany` to the list of records of the current Item with an additional parameter `mappedBy`.
This parameter contains the name of the owner Item attribute on which the `manyToOne` relation is built:
```yaml
# category
metadata:
  name: category
spec:
  attributes:
    books:
      type: relation
      relType: oneToMany
      target: book
      mappedBy: category
```

In bidirectional relationship, the owning Item must also contain an additional parameter (called `inversedBy`) on its side.
Similar to `mappedBy`, this parameter contains the name of the attribute of the opposite Item on which the `oneToMany` relationship is built:
```yaml
# book
metadata:
  name: book
spec:
  attributes:
    userInfo:
      type: relation
      relType: manyToOne
      target: category
      columnName: category_id
      inversedBy: books
```

#### manyToMany relationship

The `manyToMany` relationship implies that the current Item's records can be referenced by the second Item's records.
The reverse is also true: records of the second Item can be referenced by records of the current Item.
To model such a relationship in a relational DBMS, it is necessary to have an intermediate table that stores links of the tables of the first and second Items to each other.
Based on this requirement, before implementing the `manyToMany` relationship, we need to create an intermediate Item with two attributes `source` and `target` of type `relation` and the value `relType` = `manyToOne` (one for each of the main Items):
```yaml
# categoryBookMap
metadata:
  name: categoryBookMap
spec:
  attributes:
    source:
      type: relation
      relType: manyToOne
      target: category
      columnName: category_id
    target:
      type: relation
      relType: manyToOne
      target: book
      columnName: book_id
```

You can then create `manyToMany` relations in each of the primary Items by specifying an `intermediate` property of the relation attribute.
If the current Item is in the `categoryBookMap` in the `source` position, then the name of the attribute of the opposite Item is indicated in the `inversedBy` property; if in the `target` position - then in the `mappedBy` property:
```yaml
# category
metadata:
  name: category
spec:
  attributes:
    books:
      type: relation
      relType: manyToMany
      target: book
      intermediate: categoryBookMap
      inversedBy: categories
```

```yaml
# book
metadata:
  name: book
spec:
  attributes:
    categories:
      type: relation
      relType: manyToMany
      target: category
      intermediate: categoryBookMap
      mappedBy: books
```

The Items that make up the `manyToMany` relationship cannot belong to different data sources. This limitation is due to the presence of an intermediate Item.

Once the Item definition file has been created, it must be placed in the [schema](/src/main/resources/schema) directory (or any of its subdirectories) and the application restarted.
At startup, the system will scan all changes in the directory and apply them to the current data schema.
As a result, the Item and corresponding objects will be created in the database.
Similarly, if a file is changed, the subsequent launch of the application will also entail the necessary changes.
In addition to YAML, an Item definition can be created in JSON format ([example](/src/main/resources/schema/example-item.json)).

**The second way** to create (change) an Item is to use the `POST /api/model/apply` method. In the body we need to pass the definition of the Item in JSON format.

The OpenAPI specification for REST methods is available through the running application on the page `/swagger-ui.html`.

To use the **third method** of creating (changing) Items, we must first consider operations on Items.

## Operations

For each Item, the system generates the set of operations required for it in accordance with its name.
Below are examples of operations for the `book` Item. The first two operations in the GraphQL schema are of type `query`, the rest are `mutation`.
- `books` - returns a list of records;
- `book` - returns a specific record;
- `createBook` - creating a record;
- `createBookVersion` - creating a new version of a record for Items with the `versioned` flag (see below);
- `createBookLocalization` - creating a new record localization for Items with the `localized` flag (see below);
- `updateBook` - changing a record;
- `deleteBook` - deleting a record;
- `purgeBook` - deleting all versions of a record;
- `lockBook` - locking a record for an Item without the `notLockable` flag;
- `unlockBook` - unlocking a record for an Item without the `notLockable` flag;
- `promoteBook` - moving a record to the next stage of the life cycle (see section [Item lifecycle](lifecycle.md)).

Read-only Items have the first two methods only.

In addition to the listed methods, an Item can contain arbitrary custom methods. For more information about them, see the section [Item lifecycle](lifecycle.md).

Let's look at examples of basic operations on Item records.
On a running application, we can use the built-in version of Graph*i*QL at `/graphiql` (before executing the request, we need to add the `Authorization` header with the received token in the header field - see the [Security](security.md)).
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

When deleting, in addition to the record identifier, you must pass the `deletionStrategy` parameter.
This parameter defines the action that will be performed on related Items and has three possible values:
- `NO_ACTION` - do not take any action;
- `SET_NULL` - set the value `NULL` in the column of the related Item;
- `CASCADE` - cascade deletion of related Items.

### Retrieving Item data

Example GraphQL query:
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

The `filters` block can contain flexible filtering conditions (unified with filters used in [Strapi](https://strapi.io)) with support for nesting of attributes.
The complete list of operators is as follows (may vary for different attribute types):
- `eq` - equal;
- `ne` - not equal;
- `gt` - greater;
- `gte` - greater than or equal to;
- `lt` - less;
- `lte` - less than or equal to;
- `contains` - contains a substring;
- `containsi` - contains a substring (case-insensitive);
- `notContains` - does not contain a substring;
- `notContainsi` - does not contain a substring (case-insensitive);
- `startsWith` - starts with a substring;
- `endsWith` - ends with a substring;
- `between` - between (the operand must be an array of two elements);
- `in` - included in (operand must be an array);
- `notIn` - not included in (operand must be an array);
- `null` - equals `NULL` (the operand must be the boolean value `true` or `false`);
- `notNull` - not equal to `NULL` (the operand must be a boolean value `true` or `false`).

Filtering conditions can be combined with the `and`, `or` and `not` operators.

The `sort` field contains a list of sorting fields.
After the name, a colon can specify the sorting direction (`asc` - ascending or `desc` - descending).
By default, sorting is in ascending order.

The `pagination` block is used to configure pagination and can contain the fields `page`, `pageSize` (number starting from 1 and page size) or `start`, `limit` (number of the first record starting from 0 and the number of records).
In the response in the `meta.pagination` block, in addition to the listed parameters (`page`, `pageSize`, `start`, `limit`), we can request the fields `total` (total number of records that match the filtering condition) and `totalPages` (total number of pages).

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

If the Item is cacheable, but the data size exceeds the app configuration parameter `scicms-core.data.max-cached-records-size` (default - 200), then the data will not be cached.

After describing the main operations, we can consider the **third method** of creating/modifying/deleting Items.
The description (metadata, attributes, indexes, etc.) of any Item is the Item too (named `item`, it is stored in the main database along with other Items in the `core_items` table).
Thus, the `createItem`, `updateItem` and `deleteItem` methods are intended to manipulate Items.
They are widely used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client).
The structure of the request body is almost identical to the REST method `POST /api/model/apply`, except that the contents of `metadata` are specified directly in the `data` field.
After making a change, the GraphQL schema is automatically regenerated and the client can work with the actual version.
If multiple instances of SciCMS are running in a cloud environment, they are synchronized via the [Redis](https://redis.io) cache and each of them also contains the latest version of the schema.

## Versioning

In order for an Item to be versioned, it is necessary to set the `metadata.versioned` parameter to `true`.
In this case, when generating the GraphQL API, it will receive new methods `createVersion` (create a new version of a record) and `purge` (delete all versions of a record).
The `createVersion` method can take an additional `copyCollectionRelations` parameter.
If it is passed as `true`, then copies of records in the related tables for the `oneToMany` and `manyToMany` attributes will be created.
Versioned Items do not have an `update` method, i.e. records are not updated (new versions are always created).

The `majorRev` attribute is responsible for assigning versions.
It can be set automatically or manually (if the Item has the `metadata.manualVersioning` parameter set).
With automatic assigning, the so-called revision policy is responsible for this action.
This is a separate `revisionPolicy` Item (its records are stored in the `core_revision_policies` table), which for each policy has a `revisions` text field that defines the composition and order of versions.

When manually numbering, a mandatory parameter `majorRev` is added to the request.
Also, each versioned record has a `minorRev` attribute. It is optional and can be set by the user in the `data` block of the request.
An example of a request to create a new version (the identifier of the original record must be transmitted):
```
mutation {
  createBookVersion(
    id: "d160cdfd-e548-412f-9249-d27de0274499"
    majorRev: "A"
    data: {
      name: "Alice's Adventures in Wonderland"
      rating: 9
      minorRev: "0"
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

In the search request, the `majorRev` attribute is added to versioned Items.

## Localization

In order for an Item to be multilingual, it is necessary to set the `metadata.localized` parameter to `true`.
In this case, when generating the GraphQL API, it will receive a new `createLocalization` method with the `locale` parameter.
The passed locale value must match the `name` attribute of an existing record of the same name `locale` Item (its records are stored in the `core_locales` table).
An example of a request to create a new localization (the identifier of the original record must be passed):
```
mutation {
  createBookLocalization(
    id: "d160cdfd-e548-412f-9249-d27de0274499"
    locale: "ru"
    data: {
      name: "Алиса в Стране Чудес"
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

In the search query for multilingual Items, the `locale` attribute is added.

## Data sources

Like any other Item, a data source is created using a GraphQL query:
```
mutation {
  createDatasource(
    data: {
      name: "acc"
      connectionString: "jdbc:postgresql://127.0.0.1:5432/accounting"
      username: "${ACC_DB_USERNAME}"
      password: "${ACC_DB_PASSWORD}"
    }
  ) {
    data {
      id
      name
    }
  }
}
```

The database username and password can be specified directly or using environment variables (as in the example).
Despite the fact that the password cannot be read through the external API, in the SciCMS database the credentials of the data sources are stored in pure form (since they are used when opening connections).
Therefore, it is recommended to use the second method (environment variables).
Once created, the data source can be used by anyone to define an Item by its name.

The entire described API is also used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client), which provides a convenient user interface for managing Items, as well as many other features.