package com.github.georgenady.rettrofitapigraph.data.service

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "SwaggerSettings", storages = [Storage("swagger_settings.xml")])
@Service(Service.Level.APP)
class SwaggerSettingsService : PersistentStateComponent<SwaggerSettingsService.State> {

    data class State(
        var defaultHeaders: MutableMap<String, String> = mutableMapOf("Content-Type" to "application/json")
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    companion object {
        fun getInstance(): SwaggerSettingsService = service()
    }
}
