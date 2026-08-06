---
status: accepted
---

# Own dialog operational failures

FileKit normalizes expected platform failures at each suspending dialog-operation seam and exposes them through a small FileKit-owned hierarchy rooted at `FileKitDialogException`. The existing `FileKitPickerException` remains as a subtype, while new operation-specific subtypes are added only when they enable distinct caller recovery; this prevents platform exception leakage without making the broad `FileKitException` hierarchy or incidental platform types part of every Compose launcher's error interface. Caller-controlled contract violations remain fail-fast and outside this hierarchy, while valid operations blocked by platform or environmental conditions are operational failures.

Compose launchers catch only `FileKitDialogException` during operation execution. Cancellation and consumer callback exceptions therefore continue to propagate, while compatibility overloads without `onError` retain their interface and deliberately ignore normalized operational failures without implicit logging.

Picker, directory, saver, camera, and sharing launchers adopt this explicit error interface together so callers do not need operation-specific knowledge of which callback launchers report failures.

State-tracking picker modes retain their existing failure-as-data interface: `FileKitPickerState.Failed` remains a terminal value delivered through `onResult`. Their `onError` callback is reserved for thrown operational failures that the state stream does not represent.
