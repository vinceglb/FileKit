# macOS JVM Main-Thread Dispatch

This context describes FileKit's macOS JVM main-thread dispatch language. It exists to distinguish work and state owned by one FileKit runtime from similarly named state owned elsewhere in the process.

## Language

**Runnable Adapter**:
A FileKit-owned bridge that carries a JVM runnable ticket onto the macOS application thread.
_Avoid_: IdeaRunnable, global helper

**Runnable Ticket**:
The opaque identifier that connects one scheduled main-thread callback to the corresponding JVM work.
_Avoid_: Runnable pointer, callback state

**Foreign Adapter**:
A main-thread bridge owned by code outside the current FileKit runtime owner, even when its name or behavior appears compatible.
_Avoid_: Reusable adapter, shared helper

**Runtime Owner**:
The FileKit runtime instance that owns one runnable adapter and its pending runnable tickets as a single lifecycle.
_Avoid_: Global helper, shared adapter
