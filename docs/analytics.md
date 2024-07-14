# Analytics tools

Access to analytical information is provided via REST API.
The OpenAPI specification for all methods is available through the running application at `/swagger-ui.html`.

## Data sources

Creating data sources is described in the [Data Model](data_model.md) section. Here we will consider additional operations in the context of analytical tasks.
In particular, working with a data source requires knowing what tables it contains and what the structure of those tables is.

### Getting information about tables

To obtain detailed information about the tables contained in the data source, use the `GET /api/datasource/<datasource_name>/tables` method, where `<datasource_name>` is the name of the data source.
The following parameters can also be passed in the query string (to build a query string from JavaScript code, we can use the [qs](https://github.com/ljharb/qs) library):
- `schema` - name of the database schema (by default, the current schema of the user on whose behalf the connection is opened is used);
- `q` - filter string for the table name by partial match (case independent);
- `pagination` - pagination parameters (similar to those used in [Item methods](data_model.md "Data Model")).

Sample answer:
```json
{
  "data": [
    {
      "name": "books",
      "columns": {
        "id": {
          "type": "string"
        },
        "rating": {
          "type": "int"
        }
      }
    }
  ],
  "meta": {
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "pageCount": 1
    }
  }
}
```

The `type` field is derived from the actual data type of the column in the database and is included in the subset of valid [attribute types](data_model.md "Data Model") for the Item.
Can take the following values:
- `string` - fixed-length string;
- `text` - string of unlimited length;
- `int` - integer type;
- `long` - long integer type;
- `float` - floating-point numeric type;
- `double` - double precision floating point numeric type;
- `decimal` - type for a decimal number;
- `date` - type for storing dates;
- `time` - type for storing time;
- `timestamp` - type for storing date and time;
- `bool` - logical type.

## Datasets

In SciCMS Core, a dataset corresponds to a table or an arbitrary SQL query in a database.
Creating/updating/deleting a dataset is no different from a similar GraphQL operation with any other Item.

### Creating a dataset

```
mutation {
  createDataset(
    data: {
      name: "orders",
      datasource: "d17966c3-ea6c-4394-aef5-c9ac5c93d5bd",
      tableName: "orders",
      cacheTtl: 10
      spec: {
        columns: {
          o_orderkey: {
            hidden: true
          },
          o_total_price: {
            hidden: true
          },
          o_comment: {
            hidden: true
          },
          orders_cnt: {
            custom: true,
            aggregate: "count",
            source: "o_orderkey"
          }
        }
      }
    }
  ) {
    data {
      name
      datasource {
        data {
          id
          name
        }
      }
      tableName
    }
  }
}
```

The `name` field contains the name of the dataset (in Latin letters without spaces), it is used when executing queries (see below).
The `datasource` field contains the data source identifier.

One of two parameters must be passed: `tableName` (table name) or `query` (arbitrary SQL query).
If both are passed, then when accessing the dataset, the `tableName` parameter has priority.

Just like the Item, the dataset has an optional parameter `cacheTtl` - cache lifetime in minutes.
If it is not specified, then the default value is 5 minutes (the `scicms-core.data.dataset-query-result-entry-ttl-minutes` parameter in the [application.yml](/src/main/resources/application.yml)).
If the cache value is less than or equal to 0, then records in the dataset are not cached. The cache is reset every time the application is restarted.

The `spec` field contains descriptions of the columns in the `columns` subfield. The attribute name acts as a key. The value may include the following properties:
- `custom` - the data source can contain so-called custom (virtual) columns, which are used when building analytical queries (see below); for custom columns this property is set to `true`;
- `hidden` - some columns may be excluded from the query; in this case, this property is set to `true`;
- `aggregate` - name of the aggregate function, can take values `count`, `countd`, `sum`, `avg`, `min`, `max` (see below); only applies to custom columns;
- `formula` - arbitrary SQL expression for performing more complex aggregations (see below); applies only to custom columns; if the `aggregate` and `formula` properties are specified, the first takes precedence;
- `source` - the name of the real column if the column is custom and the `formula` property is not set;
- `alias` - column alias for display in the user interface, used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client);
- `format` - column value format for display in the user interface, used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client); can take values `int`, `float`, `date`, `time`, `datetime`;
- `colWidth` - column width for display in the user interface, used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client).

Changing the dataset is performed using the `updateDataset` operation with similar GraphQL query parameters.

### Fetching data

To request data from a dataset, use the `GET /api/dataset/<dataset_name>` method, where `<dataset_name>` is the name of the dataset.
The following parameters can also be passed in the query string (to build a query string from JavaScript code, you can use the [qs](https://github.com/ljharb/qs) library):
- `fields` - list of fields (see below);
- `filters` - block of filters (the structure is similar to that used for [filtering Items](data_model.md "Data Model"), but operators must be preceded by the `$` symbol), see the example below;
- `sort` - list of sorting fields (similar to that used in [Item methods](data_model.md "Data Model")).
- `pagination` - pagination parameters (similar to those used in [Item methods](data_model.md "Data Model")).

Each field in the `fields` list has a structure that includes a subset of the dataset column properties: `name`, `type`, `custom`, `source`, `aggregate`, `formula`.

Below is an example of a query to the [TPC-H](https://www.tpc.org/tpch) database, designed for testing the performance of SQL queries:
```json
{
  "fields": [
    {
      "name": "o_custkey"
    },
    {
      "name": "o_orderstatus"
    },
    {
      "name": "orders_cnt",
      "custom": true,
      "aggregate": "count",
      "source": "o_orderkey"
    }
  ],
  "filters": {
    "orders_cnt": {
      "$gte": 10
    }
  }
}
```

This simple query calculates the number of orders by customer and order status, selecting only those records where the calculated value is greater than or equal to 10.
We can filter by both regular fields and aggregates.
If the `fields` field is not passed in the request, then the column configuration from the dataset specification is used.
If a column has the `hidden` flag, then it does not participate in the query.
Therefore, the same result can be obtained by not passing `fields` and using the specification described above for the `createDataset` method.

Instead of `aggregate` and `source`, you can pass a `formula` field, which can contain an arbitrary expression for aggregation, for example, `COUNT(DISTINCT([id]))` (same as `countd` aggregate).
Table column names in such expressions must be enclosed in square brackets.

Sample answer:
```json
{
  "data": [
    {
      "orders_cnt": 10,
      "o_orderstatus": "F",
      "o_custkey": 712
    },
    {
      "orders_cnt": 10,
      "o_orderstatus": "F",
      "o_custkey": 433
    },
    {
      "orders_cnt": 10,
      "o_orderstatus": "O",
      "o_custkey": 715
    }
  ],
  "query": "SELECT COUNT(t0.o_orderkey) AS orders_cnt,t0.o_orderstatus,t0.o_custkey FROM orders t0 GROUP BY t0.o_orderstatus,t0.o_custkey HAVING (COUNT(t0.o_orderkey) > :countt0o_orderkey_0_gte) OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY",
  "params": {
    "countt0id_0_gte": 10
  },
  "timeMs": 1,
  "cacheHit": true,
  "meta": {
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 132,
      "pageCount": 7,
      "timeMs": 2,
      "cacheHit": true
    }
  }
}
```

The `data` block contains the found records, the `meta` block contains information about pagination.
In addition to them, the response contains such fields as `query` (text of the actual query in the database), `params` (query parameters), `cacheHit` (cache hit flag) and `timeMs` (query execution time in milliseconds).
These fields are used to analyze query performance. If the dataset is cacheable, but the data size exceeds the application configuration parameter `scicms-core.data.max-cached-records-size` (default - 200), then the data will not be cached.

Based on the described tools, the client application [SciCMS Client](https://github.com/borisblack/scicms-client) implements a business analysis module that provides a convenient user interface for managing data sources and datasets, building visualization panels, as well as many other functions.
