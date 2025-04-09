# ServerStatus Plugin

ServerStatus is a Minecraft plugin designed to integrate with Discord using DiscordSRV. It provides real-time updates about the server's status and online players in Discord channels.

## Features

- Displays server status (e.g., loading, active, offline) in a Discord channel.
- Updates the online player count in a separate Discord channel.
- Supports loading animations during server startup.
- Provides commands for reloading configuration and synchronizing channels.
- Fully configurable via `plugin.yml` and configuration files.

## Requirements

- **Minecraft Server**: Paper 1.21 or higher.
- **Java**: Version 21 or higher.
- **DiscordSRV**: Version 1.28.0 or higher.

## Installation

1. Download the plugin JAR file from the releases section.
2. Place the JAR file in the `plugins` folder of your Minecraft server.
3. Start the server to generate the default configuration files.
4. Configure the plugin by editing the generated configuration files.

## Configuration

The plugin's configuration is located in the `config.yml` file. You can customize the following:

- **Discord Channel IDs**: Specify the channels for status and online player updates.
- **Loading Animation**: Define frames, intervals, and duration for the loading animation.
- **Titles**: Customize the titles for active, offline, and online statuses.

Example configuration:
```yaml
channels:
  status: "123456789012345678"
  online: "987654321098765432"

animation:
  loading:
    - "Loading..."
    - "Starting up..."
    - "Almost ready..."
  interval: 20
  duration: 120

titles:
  active: "Server is Active"
  offline: "Server is Offline"
  online_format: "Online: {online}/{max}"
```

## Commands

- `/serverstatus reload`  
  Reloads the plugin configuration.  
  **Permission**: `serverstatus.reload`

- `/serverstatus sync`  
  Synchronizes the Discord channels with the current server status.  
  **Permission**: `serverstatus.sync`

## Permissions

- `serverstatus.reload`: Allows reloading the plugin configuration.
- `serverstatus.sync`: Allows synchronizing Discord channels.

## Building the Plugin

To build the plugin from source:

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd ServerStatus
   ```

2. Build the project using Maven:
   ```bash
   mvn clean package
   ```

3. The compiled JAR file will be located in the `target` directory.

## Dependencies

The plugin depends on the following libraries:

- [Paper API](https://papermc.io/)
- [DiscordSRV](https://github.com/DiscordSRV/DiscordSRV)

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Author

- **Kxysl1k**  
  [Website](https://kxysl1k.netlify.app/)
