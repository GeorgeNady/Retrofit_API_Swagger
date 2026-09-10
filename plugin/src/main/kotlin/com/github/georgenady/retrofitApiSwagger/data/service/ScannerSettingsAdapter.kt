package com.github.georgenady.retrofitApiSwagger.data.service

import com.github.georgenady.retrofitApiSwagger.scanner.ScannerSettings
import com.intellij.openapi.project.Project

class ScannerSettingsAdapter(
    private val project: Project
) : ScannerSettings {

    private val settingsService get() = SwaggerSettingsService.getInstance(project)

    override val isKotlinEnabled: Boolean
        get() = settingsService.state.scanKotlinFiles

    override val isJavaEnabled: Boolean
        get() = settingsService.state.scanJavaFiles
}
