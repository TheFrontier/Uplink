# Uplink Data

This branch is where all official display data is stored. In the future, servers will be able to send a new url for client mods to use.

**Note:** All files must be named as specified: `<client id>.json`

## Data Specifications

### Server

This section houses the data that is polled to display the server a user is currently on.

Files stored here must be named `<client id>.json`, and must contain only an array at the root, which contains 1 or more `Server Object`s.

#### Server Object:

| Field | Type   | Description                                                        |
|-------|--------|--------------------------------------------------------------------|
| uid   | string | The exact IP address the user connects to.                       |
| key   | string | The image key used to fetch the image from Discord's servers.      |
| name  | string | The server address info to display when the image is hovered over. |

#### Example File:

```json
[
  {
    "uid": "demo.spongepowered.org",
    "key": "server-demo_spongepowered_org",
    "name": "demo.spongepowered.org"
  }
]
```