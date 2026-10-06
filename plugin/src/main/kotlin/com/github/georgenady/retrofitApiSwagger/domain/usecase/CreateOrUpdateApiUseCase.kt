package com.github.georgenady.retrofitApiSwagger.domain.usecase

import com.github.georgenady.retrofitApiSwagger.domain.model.ApiParamPayload
import com.github.georgenady.retrofitApiSwagger.domain.model.CreateOrUpdateApiRequest
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

class CreateOrUpdateApiUseCase(private val project: Project) {

    operator fun invoke(targetFile: VirtualFile, request: CreateOrUpdateApiRequest): Result<Unit> {
        return runCatching {
            val psiFile = PsiManager.getInstance(project).findFile(targetFile)
                ?: error("Could not find PSI file for ${targetFile.path}")

            WriteCommandAction.runWriteCommandAction(project, "Create/Update Retrofit API", null, Runnable {
                when (psiFile) {
                    is KtFile -> handleKotlinFile(psiFile, request)
                    is PsiJavaFile -> handleJavaFile(psiFile, request)
                    else -> error("Unsupported file type: ${psiFile.fileType.name}")
                }
            })
        }
    }

    private fun handleKotlinFile(ktFile: KtFile, request: CreateOrUpdateApiRequest) {
        val psiFactory = KtPsiFactory(project)

        val targetClass = ktFile.declarations.filterIsInstance<KtClass>()
            .firstOrNull { it.isInterface() }
            ?: ktFile.declarations.filterIsInstance<KtClass>().firstOrNull()
            ?: error("No interface or class found in ${ktFile.name}")

        val paramsCode = request.parameters.joinToString(", ") { p ->
            val cleanName = p.name.trim().ifEmpty { "param" }
            val cleanType = p.type.trim().ifEmpty { "String" }
            val anno = when (p.location.uppercase()) {
                "PATH" -> "@Path(\"$cleanName\")"
                "QUERY" -> "@Query(\"$cleanName\")"
                "HEADER" -> "@Header(\"$cleanName\")"
                "BODY" -> "@Body"
                else -> "@Query(\"$cleanName\")"
            }
            "$anno $cleanName: $cleanType"
        }

        val suspendPrefix = if (request.isSuspend) "suspend " else ""
        val returnSuffix = if (request.returnType.isNotBlank()) ": ${request.returnType.trim()}" else ""
        val cleanPath = request.path.trim().removePrefix("/").let { if (it.isEmpty()) "/" else it }

        val functionCode = """
            @${request.httpMethod.uppercase()}("$cleanPath")
            ${suspendPrefix}fun ${request.methodName.trim()}($paramsCode)$returnSuffix
        """.trimIndent()

        if (request.isUpdate) {
            val functions = targetClass.declarations.filterIsInstance<KtNamedFunction>()
            val existing = functions.find { fn ->
                val nameMatch = fn.name == request.methodName || 
                    (request.originalSignature != null && request.originalSignature.contains(fn.name ?: ""))
                nameMatch
            } ?: functions.firstOrNull { it.name == request.methodName }

            if (existing != null) {
                val newFunction = psiFactory.createFunction(functionCode)
                val replaced = existing.replace(newFunction)
                CodeStyleManager.getInstance(project).reformat(replaced)
            } else {
                insertNewFunction(targetClass, psiFactory, functionCode)
            }
        } else {
            insertNewFunction(targetClass, psiFactory, functionCode)
        }

        ensureKotlinImports(ktFile, psiFactory, request)
    }

    private fun insertNewFunction(targetClass: KtClass, psiFactory: KtPsiFactory, functionCode: String) {
        val newFunction = psiFactory.createFunction(functionCode)
        val body = targetClass.getBody() ?: (targetClass.add(psiFactory.createEmptyClassBody()) as org.jetbrains.kotlin.psi.KtClassBody)
        val rBrace = body.rBrace
        val added = if (rBrace != null) {
            body.addBefore(newFunction, rBrace)
        } else {
            body.add(newFunction)
        }
        CodeStyleManager.getInstance(project).reformat(added)
    }

    private fun ensureKotlinImports(ktFile: KtFile, psiFactory: KtPsiFactory, request: CreateOrUpdateApiRequest) {
        val fileText = ktFile.text
        val isKtorfit = fileText.contains("de.jensklingenberg.ktorfit") || fileText.contains("ktorfit")
        val basePackage = if (isKtorfit) "de.jensklingenberg.ktorfit.http" else "retrofit2.http"

        val importsToAdd = mutableListOf("$basePackage.${request.httpMethod.uppercase()}")

        request.parameters.forEach { param ->
            when (param.location.uppercase()) {
                "PATH" -> importsToAdd.add("$basePackage.Path")
                "QUERY" -> importsToAdd.add("$basePackage.Query")
                "HEADER" -> importsToAdd.add("$basePackage.Header")
                "BODY" -> importsToAdd.add("$basePackage.Body")
            }
        }

        val existingImports = ktFile.importDirectives.mapNotNull { it.importPath?.pathStr }.toSet()

        importsToAdd.distinct().forEach { fqn ->
            if (fqn !in existingImports) {
                try {
                    val importDirective = psiFactory.createImportDirective(ImportPath(FqName(fqn), false))
                    ktFile.importList?.add(importDirective)
                } catch (_: Throwable) {}
            }
        }
    }

    private fun handleJavaFile(javaFile: PsiJavaFile, request: CreateOrUpdateApiRequest) {
        val factory = JavaPsiFacade.getElementFactory(project)
        val targetClass = javaFile.classes.firstOrNull { it.isInterface }
            ?: javaFile.classes.firstOrNull()
            ?: error("No interface found in ${javaFile.name}")

        val paramsCode = request.parameters.joinToString(", ") { p ->
            val cleanName = p.name.trim().ifEmpty { "param" }
            val cleanType = p.type.trim().ifEmpty { "String" }
            val anno = when (p.location.uppercase()) {
                "PATH" -> "@Path(\"$cleanName\")"
                "QUERY" -> "@Query(\"$cleanName\")"
                "HEADER" -> "@Header(\"$cleanName\")"
                "BODY" -> "@Body"
                else -> "@Query(\"$cleanName\")"
            }
            "$anno $cleanType $cleanName"
        }

        val returnType = request.returnType.ifBlank { "retrofit2.Call<okhttp3.ResponseBody>" }
        val methodCode = """
            @${request.httpMethod.uppercase()}("${request.path}")
            $returnType ${request.methodName}($paramsCode);
        """.trimIndent()

        val newMethod = factory.createMethodFromText(methodCode, targetClass)

        if (request.isUpdate) {
            val existing = targetClass.methods.find { it.name == request.methodName }
            if (existing != null) {
                val replaced = existing.replace(newMethod)
                CodeStyleManager.getInstance(project).reformat(replaced)
            } else {
                val added = targetClass.add(newMethod)
                CodeStyleManager.getInstance(project).reformat(added)
            }
        } else {
            val added = targetClass.add(newMethod)
            CodeStyleManager.getInstance(project).reformat(added)
        }
    }
}
