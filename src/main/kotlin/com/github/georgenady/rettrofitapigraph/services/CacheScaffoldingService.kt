package com.github.georgenady.rettrofitapigraph.services

import com.github.georgenady.rettrofitapigraph.model.ApiNode
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

@Service(Service.Level.PROJECT)
class CacheScaffoldingService(private val project: Project) {

    private val psiFactory = KtPsiFactory(project)

    fun setupCacheDependency(source: ApiNode, target: ApiNode) {
        WriteCommandAction.runWriteCommandAction(project) {
            val keyName = generateKeyName(source)
            ensurePreferenceKeyExists(keyName)

            // 1. Annotate Source with @SupportCache
            annotateWithSupportCache(source, keyName)

            // 2. Annotate Target with @InvalidateCache
            annotateWithInvalidateCache(target, keyName)
        }
    }

    private fun generateKeyName(node: ApiNode): String {
        return node.methodName.uppercase() + "_CACHE_KEY"
    }

    private fun ensurePreferenceKeyExists(keyName: String) {
        val scope = GlobalSearchScope.allScope(project)
        val classes = PsiShortNamesCache.getInstance(project).getClassesByName("PreferenceKey", scope)
        val preferenceKeyClass = classes.firstOrNull() ?: return

        if (preferenceKeyClass is KtClass && preferenceKeyClass.isEnum()) {
            val body = preferenceKeyClass.getBody()
            if (body != null) {
                val exists = body.declarations.any { it.name == keyName }
                if (!exists) {
                    val entry = psiFactory.createEnumEntry(keyName)
                    // Simplified insertion logic
                    body.addBefore(entry, body.lastChild)
                }
            }
        }
    }

    private fun annotateWithSupportCache(node: ApiNode, keyName: String) {
        val element = node.psiElement as? KtFunction ?: return
        
        // Remove existing if any
        element.annotationEntries.find { it.shortName?.asString() == "SupportCache" }?.delete()
        
        val annotationText = "@SupportCache(key = PreferenceKey.$keyName, cacheDuration = 3600, cacheDurationUnit = CacheDurationUnit.SECONDS)"
        val annotation = psiFactory.createAnnotationEntry(annotationText)
        element.addAnnotationEntry(annotation)
    }

    private fun annotateWithInvalidateCache(node: ApiNode, keyName: String) {
        val element = node.psiElement as? KtFunction ?: return
        
        // Find existing InvalidateCache to append or create new
        val existing = element.annotationEntries.find { it.shortName?.asString() == "InvalidateCache" }
        
        if (existing != null) {
            // Logic to append to array would go here (complex PSI manipulation)
            // For scaffolding, we replace
            existing.delete()
        }
        
        val annotationText = "@InvalidateCache(keys = [PreferenceKey.$keyName])"
        val annotation = psiFactory.createAnnotationEntry(annotationText)
        element.addAnnotationEntry(annotation)
    }
}
