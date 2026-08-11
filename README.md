# Stator

An Annotation Processor with API Hooks for cross-loader mods

## How do I get it?

Stator is available on [Modrinth](https://modrinth.com/mod/stator). Alone, Stator does not do anything. If a mod requires Stator, find an appropriate
implementation for the loader.

## Version Support

| MC Version | Stator Version |
|------------|----------------|
| 26.1-26.2  | ✅ `1.0.0`      |
| < 26.1     | ❌ Incompatible |

## Dynamo

Dynamo is the official implementation of Stator. The repository can be found [here](https://github.com/Ramixin/Dynamo).


## Processors

All processors create metafiles at compile time that Stator implementations are expected to read and process.

### Entrypoints

Mods can define entrypoints by using the `@Entrypoint` annotation on `static` methods. These methods can take in several
different arguments in any order to assist with initialization.

### Events

Events can be defined by using an event annotation on a `static` method. Event annotations are themselves annotated with
the `StatorEventAnnotation` annotation, and they are assigned their respective context type and return type through that
annotation.

### Dispatchers

Dispatchers define how an event is registered on a given loader. A dispatcher method must be `static` and annotated with
the `Dispatcher` annotation. The method must take in an `Event` that matches the context and return type of the given
event annotation.

# License

This project is licensed under the MIT License