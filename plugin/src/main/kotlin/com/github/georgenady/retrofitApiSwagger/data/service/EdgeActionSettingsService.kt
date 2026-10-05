package com.github.georgenady.retrofitApiSwagger.data.service

import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.ActionPlacement
import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.EdgeActionConfig
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "EdgeActionSettings", storages = [Storage("retrofit_api_swagger_edge_actions.xml")])
@Service(Service.Level.PROJECT)
class EdgeActionSettingsService : PersistentStateComponent<EdgeActionSettingsService.State> {

    data class State(
        var actions: MutableList<EdgeActionConfig> = createDefaultActions()
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
        if (myState.actions.isEmpty()) {
            myState.actions = createDefaultActions()
        }
    }

    companion object {
        fun getInstance(project: Project): EdgeActionSettingsService = project.service()

        fun createDefaultActions(): MutableList<EdgeActionConfig> = mutableListOf(
            EdgeActionConfig(
                id = "cache_invalidation",
                name = "Setup Cache Invalidation",
                description = "Annotate target with @InvalidateCache pointing to source cache key",
                icon = "⚡",
                placement = ActionPlacement.ANNOTATE_TARGET,
                template = "@InvalidateCache(keys = [PreferenceKey.\${source.methodName.toUpperCase()}_CACHE_KEY])"
            ),
            EdgeActionConfig(
                id = "support_cache",
                name = "Mark Source as Cached",
                description = "Annotate source with @SupportCache",
                icon = "💾",
                placement = ActionPlacement.ANNOTATE_SOURCE,
                template = "@SupportCache(key = PreferenceKey.\${source.methodName.toUpperCase()}_CACHE_KEY, cacheDuration = 3600)"
            ),
            EdgeActionConfig(
                id = "flow_chain_stub",
                name = "Generate Flow Chaining Comment",
                description = "Injects a reactive Coroutine Flow pipeline comment above the target API",
                icon = "🔗",
                placement = ActionPlacement.INJECT_BEFORE_TARGET,
                template = """
                    // Chained Call: ${'$'}{source.methodName}() -> ${'$'}{target.methodName}()
                    // api.${'$'}{source.methodName}().flatMapLatest { result -> api.${'$'}{target.methodName}(result) }
                """.trimIndent()
            )
        )
    }
}
