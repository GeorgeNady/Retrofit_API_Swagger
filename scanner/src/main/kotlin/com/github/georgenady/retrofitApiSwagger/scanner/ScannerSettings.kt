package com.github.georgenady.retrofitApiSwagger.scanner

/**
 * Configuration contract defining file scanning options for the scanner module.
 */
interface ScannerSettings {

    /**
     * Whether Kotlin files (*.kt) should be scanned. Defaults to `true`.
     */
    val isKotlinEnabled: Boolean

    /**
     * Whether Java files (*.java) should be scanned. Defaults to `true`.
     */
    val isJavaEnabled: Boolean
}
