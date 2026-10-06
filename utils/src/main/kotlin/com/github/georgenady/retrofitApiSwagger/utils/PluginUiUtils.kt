package com.github.georgenady.retrofitApiSwagger.utils

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

/**
 * Common UI utility functions for IDE plugins.
 */
object PluginUiUtils {

    /**
     * Opens the IDE Settings/Preferences dialog navigated to the Plugins manager.
     */
    fun openPluginsSettings(project: Project?) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Plugins")
    }

    /**
     * Opens an external web URL safely in the user's default browser.
     */
    fun openBrowser(url: String) {
        BrowserUtil.browse(url)
    }
}
