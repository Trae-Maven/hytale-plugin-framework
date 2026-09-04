# Hytale-Plugin-Framework

A Hytale server plugin framework providing structured command systems, event utilities, packet interception, ECS integration, and lifecycle management built on the [Hierarchy-Framework](https://github.com/Trae-Maven/hierarchy-framework).

Hytale-Plugin-Framework bridges the Hytale plugin lifecycle with the component-based hierarchy architecture, automatically handling registration and teardown of listeners, packet watchers, packet filters, commands, subcommands, and ECS event systems as components are initialized and shut down.

---

## Features

- Automatic Hytale registration — listeners, packet watchers, packet filters, commands, subcommands, and ECS systems are registered/unregistered through hierarchy lifecycle callbacks
- Packet interception — inbound and outbound packet watchers and filters with automatic pipeline registration and deregistration
- ECS event system integration — custom entity and chunk event systems with a unified `SystemContext` API
- Command system that transparently overrides built-in system commands, restoring them on shutdown
- Thread-safe event dispatch utilities — synchronous and asynchronous with `CompletableFuture` support
- Custom event base classes with cancellation reasons
- World-thread-aware task execution with `CompletableFuture` bridging
- Internal plugin registry for framework-managed plugin lookup
- Designed for modern Java (Java 25+)

---

## Hierarchy

```
HytalePlugin (extends JavaPlugin, implements Plugin)
  └─ Manager
       └─ BaseCommand (Node under the Manager)
            └─ BaseSubCommand (Node under the command)
```

Commands and subcommands integrate directly into the hierarchy as Nodes, each with typed access to its parent:

| Component | Hierarchy Role | Hytale Integration |
|---|---|---|
| `HytalePlugin` | Plugin | `JavaPlugin` lifecycle, component registration |
| `Manager` | Manager | Organizational grouping |
| `BaseCommand` | Node under a Manager | Registered with the command registry |
| `BaseSubCommand` | Node under a command | Attached to parent command |

---

## Requirements

Hytale-Plugin-Framework requires Java 25+ and the Hytale Server API.

The following is only needed at compile time for annotation processing:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.44</version>
    <scope>provided</scope>
</dependency>
```

---

## Built-in Dependencies

Hytale-Plugin-Framework depends on the following libraries, which are included automatically through Maven:

- [Hierarchy-Framework](https://github.com/Trae-Maven/hierarchy-framework) – Plugin, Manager, and Node hierarchy with lifecycle management.
- [Dependency Injector](https://github.com/Trae-Maven/dependency-injector) – Container management, classpath scanning, and component wiring.
- [Utilities](https://github.com/Trae-Maven/utilities) – Generic type resolution, string utilities, and casting helpers.

---

## Installation

Add the dependency to your Maven project:
```xml
<dependencies>
    <dependency>
        <groupId>io.github.trae</groupId>
        <artifactId>hytale-plugin-framework</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>
```

---

## Quick Start

### Defining the Plugin

Extend `HytalePlugin` to get automatic listener, packet watcher, command, subcommand, and ECS system registration:
```java
@Application
public class CorePlugin extends HytalePlugin {

    public CorePlugin(@Nonnull final JavaPluginInit javaPluginInit) {
        super(javaPluginInit);
    }

    @Override
    public void setup() {
        this.initializePlugin();
    }

    @Override
    public void shutdown() {
        this.shutdownPlugin();
    }
}
```

### Defining a Listener

Extend `EventListener` and annotate handler methods with `@EventHandler`:
```java
@Singleton
public class PlayerListener extends EventListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join
    }
}
```

### Defining a Command

Extend `BaseCommand`, naming the owning Manager as the second type parameter. The backing engine
wrapper is built during initialization, so aliases can be added in the constructor:
```java
@Singleton
public class AccountCommand extends BaseCommand<CorePlugin, AccountManager, CommandSender> {

    public AccountCommand() {
        super("account", "Account management", "core.commands.account");

        this.getAliases().add("acc");
    }
}
```

Commands are queued as they initialize and registered in a single pass once the whole hierarchy
is up. Registering a command displaces any built-in system command sharing its label or aliases;
the built-in is restored when the framework command is unregistered.

### Defining a SubCommand

The second type parameter names the parent command, which is resolved through `getParent()`.
Subcommands are attached to that parent automatically as each component initializes:
```java
@Singleton
public class AdminSubCommand extends BaseSubCommand<CorePlugin, AccountCommand, CommandSender> {

    public AdminSubCommand() {
        super("admin", "Toggle Admin Mode", "core.commands.account.admin");
    }
}
```

### Defining an ECS Event System

Extend `CustomEntityEventSystem` or `CustomChunkEventSystem` to handle ECS events with a simplified `SystemContext`:
```java
@Singleton
public class DamageSystem extends CustomEntityEventSystem<DamageEvent> {

    public DamageSystem() {
        super(DamageEvent.class);
    }

    @Override
    public void onEvent(DamageEvent event, SystemContext<EntityStore> context) {
        // Access components via context.getComponent(...)
        // Queue mutations via context.addComponent(...)
    }
}
```

### Event Dispatch

Use `UtilEvent` for thread-safe event dispatch:
```java
// Synchronous — fire and inspect
MyEvent event = UtilEvent.supply(new MyEvent());
if (event.isCancelled()) {
    return;
}

// Asynchronous — fire and forget
UtilEvent.dispatchAsynchronous(new MyAsyncEvent());

// Asynchronous — fire and chain
UtilEvent.supplyAsynchronous(new MyAsyncEvent()).thenAccept(e -> System.out.println("Done: " + e.isCancelled()));
```

### Task Execution

Use `UtilTask` for thread-aware task execution:
```java
// Execute on a world's thread with CompletableFuture result
CompletableFuture<BlockData> completableFuture = UtilTask.supplyByWorld(world, () -> {
    return world.getBlockAt(x, y, z);
});

// Fire and forget on a world's thread
UtilTask.executeByWorld(world, () -> {
    world.setBlockAt(x, y, z, blockData);
});

// Async off the main thread
UtilTask.executeAsynchronous(() -> {
    // Heavy computation
});
```

---

## Utilities

| Utility | Description |
|---|---|
| `UtilEvent` | Synchronous and asynchronous event dispatch with supply variants |
| `UtilTask` | Thread-aware task execution — immediate, synchronous, async, and world-thread |
| `UtilPlugin` | Plugin lookup — external by identifier, internal by name or class |

---

## Event Types

| Event Type | Description |
|---|---|
| `CustomEvent` | Base synchronous event with `Void` key type |
| `CustomAsyncEvent` | Base asynchronous event with `Void` key type |
| `CustomCancellableEvent` | Synchronous event with cancellation and reason |
| `CustomCancellableAsyncEvent` | Asynchronous event with cancellation and reason |

---

## Packet Watchers and Filters

| Marker Interface | Direction | Description |
|---|---|---|
| `InboundPacketWatcher` | Client → Server | Observes packets sent by the client |
| `OutboundPacketWatcher` | Server → Client | Observes packets sent to the client |

Packet watchers implement `PacketWatcher` or `PlayerPacketWatcher` alongside a direction marker;
packet filters implement `PacketFilter` or `PlayerPacketFilter`. All four are automatically
registered with `PacketAdapters` on component initialization and deregistered on shutdown. They
run on the network thread — ECS component access must be scheduled via `world.execute()`.

---

## ECS Systems

| System Type | Store | Use Case |
|---|---|---|
| `CustomEntityEventSystem` | `EntityStore` | Entity-level ECS events |
| `CustomChunkEventSystem` | `ChunkStore` | Chunk-level ECS events |

Both system types wrap the raw `EntityEventSystem.handle(...)` parameters into a `SystemContext`, providing a clean API for component access and deferred mutations via `CommandBuffer`.

---

## Helpers

| Helper | Manages |
|---|---|
| `EventHelper` | Event listener registrations |
| `SystemHelper` | ECS system registrations |
| `CommandHelper` | Command and subcommand registrations, with built-in command override and restore |
| `PacketWatcherHelper` | Packet watcher registrations |
| `PlayerPacketWatcherHelper` | Player packet watcher registrations |
| `PacketFilterHelper` | Packet filter registrations |
| `PlayerPacketFilterHelper` | Player packet filter registrations |

---

## Interfaces

| Interface | Description |
|---|---|
| `HytalePlugin` | Root plugin with automatic Hytale registration callbacks |
| `Node` | Typed parent access for commands and subcommands (provided by Hierarchy-Framework) |
| `SharedBaseCommand` | Shared contract between commands and subcommands |
| `EventListener` | Base class for event listener discovery |
| `InboundPacketWatcher` | Marker interface for inbound packet watcher direction |
| `OutboundPacketWatcher` | Marker interface for outbound packet watcher direction |
| `Processable` | Deferred batch-processing contract for helpers |
| `ICustomEventSystem` | Unified handler contract for custom ECS event systems |
