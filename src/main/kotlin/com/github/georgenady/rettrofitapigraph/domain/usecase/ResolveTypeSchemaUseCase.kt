package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtClass

class ResolveTypeSchemaUseCase(private val project: Project) {
    operator fun invoke(fqn: String): Map<String, String> {
        val psiClass = JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.allScope(project))
            ?: return emptyMap()

        val schema = mutableMapOf<String, String>()
        psiClass.allFields.forEach { field ->
            val name = getSerializedName(field) ?: field.name
            schema[name] = field.type.presentableText
        }
        return schema
    }

    private fun getSerializedName(field: com.intellij.psi.PsiField): String? {
        val annotation = field.getAnnotation("com.google.gson.annotations.SerializedName")
            ?: field.getAnnotation("kotlinx.serialization.SerialName")
        
        return annotation?.findAttributeValue("value")?.text?.removeSurrounding("\"")
    }
}
