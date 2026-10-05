package com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model

data class EdgeActionConfig(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var icon: String = "⚡",
    var placement: ActionPlacement = ActionPlacement.ANNOTATE_TARGET,
    var template: String = "",
    var targetFileNameTemplate: String = ""
)
