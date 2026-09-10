package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

class GenerateJsonSchemaUseCase(private val project: Project) {
    operator fun invoke(fqn: String): String {
        val psiClass = JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.allScope(project))
            ?: return "{}"

        val map = mutableMapOf<String, Any?>()
        psiClass.allFields.forEach { field ->
            val jsonName = getSerializedName(field) ?: field.name
            map[jsonName] = getDefaultValue(field.type)
        }
        
        return com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(map)
    }

    private fun getSerializedName(field: PsiField): String? {
        val gsonAnno = field.getAnnotation("com.google.gson.annotations.SerializedName")
        if (gsonAnno != null) {
            return gsonAnno.findAttributeValue("value")?.text?.removeSurrounding("\"")
        }
        
        val serialNameAnno = field.getAnnotation("kotlinx.serialization.SerialName")
        if (serialNameAnno != null) {
            return serialNameAnno.findAttributeValue("value")?.text?.removeSurrounding("\"")
        }
        
        return null
    }

    private fun getDefaultValue(type: PsiType): Any? {
        val text = type.presentableText
        return when {
            text == "String" -> ""
            text == "Int" || text == "Long" -> 0
            text == "Boolean" -> false
            text == "Double" || text == "Float" -> 0.0
            type is PsiArrayType -> emptyList<Any>()
            else -> null
        }
    }
}
