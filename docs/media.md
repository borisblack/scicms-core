# Working with files

To store files, we need to set the Item to an attribute with type `media`. For more information about attribute types, see section [Data model](data_model.md).
The field of this attribute stores the identifier of the `media` Item record from `core_media` tables of the main database.
The file itself can be physically stored either in the file system or in object storage [S3](https://aws.amazon.com/s3).
File storage settings are located in the file [application.yml](/src/main/resources/application.yml) in the `scicms-core.media` block.
The `provider` property accepts two values: `local` and `s3` (by default the environment variable `MEDIA_PROVIDER` is used or the value `local` in it absence).

Storage settings in the file system are specified in the `scicms-core.media.local` block. The following properties are available:
- `base-path` - base saving path (by default the environment variable `MEDIA_LOCAL_PATH` is used);
- `create-directories` - flag for creating path directories if they do not already exist (set to `true` by default).

Storage settings in S3 object storage are specified in the `scicms-core.media.s3` block. The following properties are available:
- `endpoint` - service address (by default the environment variable `S3_ENDPOINT` or the address `http://127.0.0.1:9000` in its absence);
- `access-key` - access key (by default the environment variable `S3_ACCESS_KEY` or the value `minioadmin` is used in her absence);
- `secret-key` - secret key (by default the environment variable `S3_SECRET_KEY` or the value `minioadmin` is used in her absence);
- `default-bucket` - name of the bucket to save (by default the environment variable `S3_DEFAULT_BUCKET` or the value of `scicms` in its absence);

## File operations

### Unloading
Uploading files can be done either via REST or GraphQL API.

The first method involves sending a `POST /api/media/upload` request. The request body of type `multipart/form-data`
must contain following fields:
- `file` - uploaded file;
- `label` - display file name (optional);
- `description` - file description (optional);
- `permission` - permission identifier (optional).

The response body is in JSON format and contains data about the saved file:
```json
{
  "id": "6ee7e3fa-e5e3-4d81-92da-ddf123593a2a",
  "filename": "alice.png",
  "label": null,
  "description": null,
  "fileSize": 6984,
  "mimetype": "image/png",
  "checksum": "221f6b348cb4ba507f96d5790fa5ba9c",
  "createdAt": "2024-06-21T14:03:51.6940787+02:00"
}
```

If multiple files need to be uploaded, the `POST /api/media/upload-multiple` request is used. The request body of type
`multipart/form-data` must contain following fields:
- `files` - list of uploaded files;
- `label` - list of displayed file names in the same order as the files (optional);
- `description` - list of file descriptions in the same order as the files (optional);
- `permissions` - a list of permission IDs in the same order as the files (optional).

The response will contain a JSON array with data about the saved files (the structure is similar to the previous example).

To upload file(s) via the GraphQL API, use the `upload` and `uploadMultiple` methods.
As the only parameter these methods accept a file and a list of files, respectively (a special scalar of type `Upload` is used to represent a file in GraphQL).
The response messages are similar to the previous two REST methods.
The GraphQL protocol does not allow to pass any additional information together with an uploading file, and in this case it is taken from the attributes of the files themselves.
Therefore, it is recommended to use REST methods to upload files.

### Loading

The download is performed using the `GET /api/media/<media_id>/download` method. The `<media_id>` parameter is an identifier
of the `media` entry.

### Changing file information

The update operation is performed as standard, as for any other Item. To do this you need to run GraphQL request:
```
mutation {
  updateMedia(
    id: "6ee7e3fa-e5e3-4d81-92da-ddf123593a2a"
    deletingStrategy: NO_ACTION
    data: {
      label: "Alice"
    }
  ) {
    data {
      id
      filename
      label
    }
  }
}
```

### Deleting a file

The deletion operation is also performed as standard, as for any other Item:
```
mutation {	
  deleteMedia(
    id: "6ee7e3fa-e5e3-4d81-92da-ddf123593a2a"
    deletingStrategy: NO_ACTION
  ) {
    data {
      id
      filename
      label
    }
  }
}
```

For more information about operations with Items, see the [Data Model](data_model.md) section.

The entire described API is also used in the client application [SciCMS Client](https://github.com/borisblack/scicms-client), which provides a user-friendly user interface for file manipulation as well as many other features.