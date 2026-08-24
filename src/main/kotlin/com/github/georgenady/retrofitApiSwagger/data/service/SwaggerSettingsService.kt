package com.github.georgenady.retrofitApiSwagger.data.service

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "SwaggerSettings", storages = [Storage("retrofit_api_swagger_settings.xml")])
@Service(Service.Level.PROJECT)
class SwaggerSettingsService : PersistentStateComponent<SwaggerSettingsService.State> {

    data class State(
        var baseUrl: String = "https://api.example.com", // Added Base URL field
        var defaultHeaders: HashMap<String, String> = hashMapOf("Content-Type" to "application/json")
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun addDefaultHeader(key: String, value: String) {
        myState.defaultHeaders[key] = value
    }

    fun removeDefaultHeader(key: String) {
        myState.defaultHeaders.remove(key)
    }

    fun clearHeaders() {
        myState.defaultHeaders.clear()
    }

    companion object {
        // Accepts Project to retrieve the per-project service instance
        fun getInstance(project: Project): SwaggerSettingsService = project.service()
    }
}