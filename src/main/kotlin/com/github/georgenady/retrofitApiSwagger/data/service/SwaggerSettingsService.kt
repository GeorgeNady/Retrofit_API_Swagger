package com.github.georgenady.retrofitApiSwagger.data.service

import com.intellij.openapi.components.*

@State(name = "SwaggerSettings", storages = [Storage("swagger_settings.xml")])
@Service(Service.Level.APP)
class SwaggerSettingsService : PersistentStateComponent<SwaggerSettingsService.State> {

    data class State(
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
        fun getInstance(): SwaggerSettingsService = service()
    }
}
