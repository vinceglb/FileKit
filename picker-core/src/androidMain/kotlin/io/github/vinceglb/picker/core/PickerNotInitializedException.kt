package io.github.vinceglb.picker.core

public class PickerNotInitializedException :
    IllegalStateException("Picker not initialized on Android. Please call Picker.init(activity) first.")
