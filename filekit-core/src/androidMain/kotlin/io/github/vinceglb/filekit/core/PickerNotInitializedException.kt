package io.github.vinceglb.filekit.core

public class PickerNotInitializedException :
    IllegalStateException("Picker not initialized on Android. Please call Picker.init(activity) first.")
