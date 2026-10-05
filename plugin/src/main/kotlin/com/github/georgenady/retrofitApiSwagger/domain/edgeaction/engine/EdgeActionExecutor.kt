package com.github.georgenady.retrofitApiSwagger.domain.edgeaction.engine

import com.github.georgenady.retrofitApiSwagger.data.service.EdgeActionSettingsService
import com.github.georgenady.retrofitApiSwagger.domain.edgeaction.model.ActionPlacement
import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

class EdgeActionExecutor(private val project: Project) {
    private val engine = TemplateEvaluationEngine()

    fun execute(actionId: String, sourceNode: ApiNode, targetNode: ApiNode) {
        val settings = EdgeActionSettingsService.getInstance(project)
        val action = settings.state.actions.find { it.id == actionId }
            ?: throw IllegalArgumentException("Action not found with ID: $actionId")

        val evaluatedCode = engine.evaluate(action.template, sourceNode, targetNode)

        val targetPsi = targetNode.psiElement as? KtFunction
            ?: throw IllegalStateException("Target PSI function could not be resolved for '${targetNode.methodName}'")
        val sourcePsi = sourceNode.psiElement as? KtFunction
            ?: throw IllegalStateException("Source PSI function could not be resolved for '${sourceNode.methodName}'")

        WriteCommandAction.runWriteCommandAction(
            project,
            "Execute Edge Action: ${action.name}",
            "RetrofitSwagger",
            Runnable {
                val psiFactory = KtPsiFactory(project)

                // If template references a PreferenceKey, ensure enum entry exists
                checkAndCreatePreferenceKey(evaluatedCode, psiFactory)

                when (action.placement) {
                    ActionPlacement.ANNOTATE_TARGET -> {
                        addOrReplaceAnnotation(targetPsi, evaluatedCode, psiFactory)
                    }
                    ActionPlacement.ANNOTATE_SOURCE -> {
                        addOrReplaceAnnotation(sourcePsi, evaluatedCode, psiFactory)
                    }
                    ActionPlacement.INJECT_BEFORE_TARGET -> {
                        injectCodeBefore(targetPsi, evaluatedCode, psiFactory)
                    }
                    ActionPlacement.GENERATE_NEW_FILE -> {
                        // Future extension: File scaffolding
                    }
                }
            }
        )
    }

    private fun addOrReplaceAnnotation(function: KtFunction, annotationText: String, psiFactory: KtPsiFactory) {
        val cleanText = annotationText.trim()
        val formattedAnnotation = if (cleanText.startsWith("@")) cleanText else "@$cleanText"
        
        // Extract annotation name e.g. "InvalidateCache" from "@InvalidateCache(...)"
        val annotationName = formattedAnnotation.substringAfter("@").substringBefore("(").substringBefore(" ")
        
        // Remove existing annotation with the same short name
        function.annotationEntries.find { it.shortName?.asString() == annotationName }?.delete()

        val annotation = psiFactory.createAnnotationEntry(formattedAnnotation)
        function.addAnnotationEntry(annotation)
    }

    private fun injectCodeBefore(function: KtFunction, codeText: String, psiFactory: KtPsiFactory) {
        val parent = function.parent ?: return
        val lines = codeText.trim().lines()
        val commentText = lines.joinToString("\n") { line ->
            if (line.trim().startsWith("//") || line.trim().startsWith("/*")) line else "// $line"
        }
        val commentElement = psiFactory.createComment(commentText)
        val newline = psiFactory.createNewLine()
        parent.addBefore(commentElement, function)
        parent.addBefore(newline, function)
    }

    private fun checkAndCreatePreferenceKey(evaluatedCode: String, psiFactory: KtPsiFactory) {
        if (!evaluatedCode.contains("PreferenceKey.")) return

        val keyRegex = Regex("""PreferenceKey\.([A-Za-z0-9_]+)""")
        val matches = keyRegex.findAll(evaluatedCode)

        val scope = GlobalSearchScope.allScope(project)
        val classes = PsiShortNamesCache.getInstance(project).getClassesByName("PreferenceKey", scope)
        val preferenceKeyClass = classes.firstOrNull() as? KtClass ?: return

        if (preferenceKeyClass.isEnum()) {
            val body = preferenceKeyClass.getBody() ?: return
            for (match in matches) {
                val keyName = match.groupValues[1]
                val exists = body.declarations.any { it.name == keyName }
                if (!exists) {
                    val entry = psiFactory.createEnumEntry(keyName)
                    body.addBefore(entry, body.lastChild)
                }
            }
        }
    }
}
