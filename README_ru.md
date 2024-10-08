# SciCMS Core

Headless-система управления контентом с открытым исходным кодом.

Изначально система создавалась как интеграционная основа для управления данными из различных источников на промышленных предприятиях.
В SciCMS (сокр. от Scientific CMS) сделан упор на сопровождение высокотехнологичных производств.
В таких производствах жизненный цикл изделий отличается большим объемом данных, имеющих сложную многоуровневую структуру.
Дополнительно предъявляется ряд требований по версионированию и мультиязычности записей, а также возможности консолидации данных из различных источников.
На развитие системы повлияли такие проекты, как [Aras Innovator](https://aras.com) и [Strapi](https://strapi.io).

## Основные возможности системы

- простое и гибкое управление хранимыми структурами данных;
- широкий набор типов данных;
- GraphQL API;
- поддержка версионирования данных;
- поддержка мультиязычности;
- поддержка нескольких источников данных;
- поддержка связей между хранимыми сущностями (один-к-одному, многие-к-одному, один-ко-многим, многие-ко-многим);
- контроль доступа на уровне записей;
- механизм блокировки записей от изменения другими пользователями;
- аутентификация пользователей локально и по протоколу OAuth2;
- поддержка хранения файлов как в локальной файловой системе, так и в S3;
- хуки жизненного цикла хранимых сущностей;
- инструменты для построения аналитической отчетности (в сочетании с клиентским приложением [SciCMS Client](https://github.com/borisblack/scicms-client) составляют BI-систему);
- система готова к облачному развертыванию в несколько экземпляров с разделением общих данных и слоем кэширования.

## Начало работы

### Сборка и запуск

SciCMS Core представляет собой Kotlin-приложение на базе фреймворка Spring Boot 3.
Для сборки и запуска необходима версия Java не ниже 21. Сборка осуществляется из рабочего каталога командой `./gradlew build`.
Последующий запуск собранного JAR архива выполняется командой `java -jar <build_path>`.
Перед запуском необходимо задать следующие переменные окружения:
- MAIN_DB_URL - стандартная строка соединения JDBC (в настоящий момент поддерживаются СУБД PostgreSQL, Oracle, Microsoft SQL Server, MySQL/MariaDB, SQLite и H2);
- MAIN_DB_USERNAME - имя пользователя БД;
- MAIN_DB_PASSWORD - пароль пользователя БД;
- REDIS_HOST - адрес сервера Redis (значение по умолчанию - `127.0.0.1`);
- REDIS_PORT - порт Redis (значение по умолчанию - `6379`).

Примеры строк подключения:
- PostgreSQL - `jdbc:postgresql://127.0.0.1:5432/scicms`;
- Oracle - `jdbc:oracle:thin:@//127.0.0.1:1521/xepdb1`;
- Microsoft SQL Server - `jdbc:sqlserver://localhost:1433;databaseName=scicms;trustServerCertificate=true`;
- MySQL - `jdbc:mysql://localhost:3306/scicms?sessionVariables=sql_mode=''&jdbcCompliantTruncation=false`;
- MariaDB - `jdbc:mariadb://localhost:3306/scicms?sessionVariables=sql_mode=''&jdbcCompliantTruncation=false`;
- SQLite - `jdbc:sqlite:scicms.db?date_class=text`;
- H2 - `jdbc:h2:file:./scicms.h2`.

Если отсутствуют настроенные экземпляры СУБД и Redis, можно воспользоваться файлом [docker-compose-infra.yml](docker/docker-compose-infra.yml) для их запуска в [Docker](http://docker.io):
```shell
cd ./docker
docker-compose -f docker-compose-infra.yml up -d --build
```

В этом случае при запуске на локальном компьютере переменные окружения должны иметь следующие значения: MAIN_DB_URL = `jdbc:postgresql://127.0.0.1:5432/scicms`, MAIN_DB_USERNAME = `scicms`, MAIN_DB_PASSWORD = `scicms`.
Если используется встроенная БД (SQLite или H2), то при первом запуске создается файл по пути, указанному в строке подключения.
В последующих версиях SciCMS Core с целью упрощения локального запуска планируется добавить возможность работы приложения с кэшем в собственной памяти.

### Аутентификация для взаимодействия с API
После запуска системы на локальном компьютере взаимодействие сервера осуществляется по адресу `http://127.0.0.1:8079` (номер порта задается параметром `server.port` в файле [application.yml](src/main/resources/application.yml)).
Для взаимодействия с API необходимо выполнить аутентификацию и получить токен. Аутентификация может быть локальной либо с использованием протокола Oauth2.

При локальной аутентификации получение токена выполняется через запрос `POST /api/auth/local` с телом:
```json
{
  "username": "root", 
  "password": "master"
}
```

Учетная запись пользователя `root` с паролем `master` создается при первом запуске системы с полным набором прав.
Тело ответа содержит JWT токен и другую информацию о пользователе:
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

**Во всех последующих запросах** полученный токен должен присутствовать в заголовке `Authorization`: `Authorization: Bearer <jwt>`.

Спецификация OpenAPI по всем REST методам доступна через запущенное приложение на странице `/swagger-ui.html`.

Аутентификация по протоколу OAuth2 рассмотрена в разделе [Безопасность](/docs/ru/security.md).
В клиентском приложении [SciCMS Client](https://github.com/borisblack/scicms-client) реализованы оба способа аутентификации.

### Модель данных

Центральным понятием в SciCMS Core является **сущность** (Item).
В аналогии с объектно-ориентированным программированием сущность представляет собой класс (описание полей и методов), а запись для данной сущности - экземпляр класса (конкретные данные).
Элементы метаданных также представляют собой предопределенные сущности.
Самый простой способ - создание декларативного определения сущности в YAML (или JSON) файле (например, `book-item.yml`).
Это определение по назначению и структуре очень похоже на манифесты в экосистеме [Kubernetes](https://kubernetes.io/).
Пример определения сущности в SciCMS Core:

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

В данном примере создается сущность с именем `book` и двумя атрибутами `name` и `rating` соответственно типа `string` и `int`.
Подробнее об атрибутах сущности см. в разделе [Модель данных](/docs/ru/data_model.md).

После того как файл с определением сущности создан, его необходимо поместить в каталог [schema](/src/main/resources/schema) (или в любой из его подкаталогов) и перезапустить приложение.
При запуске система просканирует все изменения в каталоге и применит их к текущей схеме данных.
В результате будет создана сущность и соответствующие объекты в базе данных.
Аналогично, при изменении файла последующий запуск приложения также повлечет необходимые изменения.
Кроме YAML определение сущности может быть создано в JSON-формате ([пример](/src/main/resources/schema/example-item.json)).
Для каждой сущности система генерирует необходимый для нее набор операций в соответствии с ее именем.
Ниже в качестве примера приведены операции для сущности `book`. Первые две операции в схеме GraphQL имеют тип `query`, остальные - `mutation`.
- `books` - возвращает список записей;
- `book` - возвращает конкретную запись;
- `createBook` - создание записи;
- `createBookVersion` - создание новой версии записи для сущностей с флагом `versioned`;
- `createBookLocalization` - создание новой локализации записи для сущностей с флагом `localized`;
- `updateBook` - изменение записи;
- `deleteBook` - удаление записи;
- `purgeBook` - удаление всех версий записи;
- `lockBook` - блокировка записи для сущности без флага `notLockable`;
- `unlockBook` - разблокировка записи для сущности без флага `notLockable`;
- `promoteBook` - перемещение записи на следующий этап жизненного цикла (см. раздел [Жизненный цикл сущностей](/docs/ru/lifecycle.md)).

Кроме перечисленных методов сущность может содержать произвольные кастомные методы, подробнее о них см. в разделе [Жизненный цикл сущностей](/docs/ru/lifecycle.md).

Рассмотрим примеры основных операции над записями сущности.
На запущенном приложении можно воспользоваться встроенной версией Graph*i*QL по адресу `/graphiql` (перед выполнением запроса нужно в поле с заголовками добавить заголовок `Authorization` с полученным токеном).
Можно также использовать любой UI инструмент, поддерживающий работу с GraphQL ([Postman](https://www.postman.com), [Insomnia](https://insomnia.rest) и т.д.).

### Создание

Пример запроса GraphQL:
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

Ответ будет содержать данные созданной записи:

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

### Изменение

Пример запроса GraphQL:
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

### Удаление

```
mutation {
  deleteBook(
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

### Получение данных сущности

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

### Поиск и фильтрация

Пример запроса GraphQL:
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

Пример ответа:
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

Подробнее о параметрах фильтрации и постраничного вывода, а также остальных операциях см. в разделе [Модель данных](/docs/ru/data_model.md).

Весь описанный API также используется в клиентском приложении [SciCMS Client](https://github.com/borisblack/scicms-client), которое предоставляет удобный пользовательский интерфейс для управления сущностями и доступом, а также многие другие функции.

## Дополнительные ресурсы

[Модель данных](/docs/ru/data_model.md)

[Жизненный цикл сущностей](/docs/ru/lifecycle.md)

[Безопасность](/docs/ru/security.md)

[Работа с файлами](/docs/ru/media.md)

[Инструменты аналитики](/docs/ru/analytics.md)