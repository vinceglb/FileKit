# FileKit

FileKit provides multiplatform file access and system-mediated file interactions through a consistent interface.

## Dialog interactions

**Dialog operation**:
A FileKit-mediated system interaction for picking, selecting a directory, choosing a save destination, capturing media, or sharing files.
_Avoid_: Launcher operation, picker operation when referring to all dialog kinds

**Operational failure**:
An expected inability to complete a valid dialog operation, represented to callers as a FileKit-owned dialog failure rather than an incidental platform failure.
_Avoid_: Platform exception, unexpected defect

**Invalid invocation**:
A dialog request that violates a caller-controlled precondition, such as required initialization, valid arguments, or a documented argument combination. It is a caller-contract violation rather than an operational failure.
_Avoid_: Operational failure, platform failure
