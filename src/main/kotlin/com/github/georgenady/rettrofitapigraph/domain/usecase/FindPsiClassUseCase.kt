//package com.github.georgenady.rettrofitapigraph.domain.usecase
//
//import com.intellij.openapi.application.ReadAction
//import com.intellij.openapi.project.Project
//import com.intellij.psi.JavaPsiFacade
//import com.intellij.psi.PsiClass
//import com.intellij.psi.search.GlobalSearchScope
//import com.intellij.psi.search.PsiShortNamesCache
//
//class FindPsiClassUseCase(private val project: Project) {
//
//    operator fun invoke(targetType: String, interfaceClassName: String): PsiClass? {
//        if (targetType.isBlank()) return null
//
//        // 1. Strip generics and Kotlin nullability (e.g., "List<User>?" -> "List")
//        val cleanType = targetType.substringBefore("<").removeSuffix("?").trim()
//
//        // Extract package from the API node's class name (e.g., "com.api.MyService" -> "com.api")
//        val interfacePackage = interfaceClassName.substringBeforeLast(".", "")
//
//        return ReadAction.compute<PsiClass?, Throwable> {
//            val facade = JavaPsiFacade.getInstance(project)
//            val scope = GlobalSearchScope.allScope(project)
//
//            // Step 1: Try direct lookup (If it's already a Fully Qualified Name)
//            var foundClass = facade.findClass(cleanType, scope)
//
//            // Step 2: Ultimate Fallback -> Search the whole project by Short Name
//            if (foundClass == null) {
//                val shortCache = PsiShortNamesCache.getInstance(project)
//                val shortName = cleanType.substringAfterLast(".")
//
//                val possibleClasses = shortCache.getClassesByName(shortName, scope)
//
//                if (possibleClasses.isNotEmpty()) {
//                    // Prioritize class in the same package as the interface
//                    foundClass = possibleClasses.firstOrNull {
//                        val qName = it.qualifiedName ?: ""
//                        interfacePackage.isNotEmpty() && qName.startsWith(interfacePackage)
//                    } ?: possibleClasses.first()
//                }
//            }
//
//            // Step 3: Fallback for basic Java/Kotlin types (String, List, etc.)
//            if (foundClass == null && !cleanType.contains(".")) {
//                foundClass = facade.findClass("java.lang.$cleanType", scope)
//                    ?: facade.findClass("java.util.$cleanType", scope)
//            }
//
//            foundClass
//        }
//    }
//}

package com.github.georgenady.rettrofitapigraph.domain.usecase

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

class FindPsiClassUseCase(private val project: Project) {

    // Now a suspend function!
    suspend operator fun invoke(targetType: String, interfaceClassName: String): PsiClass? {
        if (targetType.isBlank()) return null

        val cleanType = targetType.substringBefore("<").removeSuffix("?").trim()
        val interfacePackage = interfaceClassName.substringBeforeLast(".", "")

        // readAction { ... } is a coroutine builder provided by the IntelliJ platform
        // It safely runs this block in the background with Read Access
        return readAction {
            val facade = JavaPsiFacade.getInstance(project)
            val scope = GlobalSearchScope.allScope(project)

            var foundClass = facade.findClass(cleanType, scope)

            if (foundClass == null) {
                val shortCache = PsiShortNamesCache.getInstance(project)
                val shortName = cleanType.substringAfterLast(".")
                val possibleClasses = shortCache.getClassesByName(shortName, scope)

                if (possibleClasses.isNotEmpty()) {
                    foundClass = possibleClasses.firstOrNull {
                        val qName = it.qualifiedName ?: ""
                        interfacePackage.isNotEmpty() && qName.startsWith(interfacePackage)
                    } ?: possibleClasses.first()
                }
            }

            if (foundClass == null && !cleanType.contains(".")) {
                foundClass = facade.findClass("java.lang.$cleanType", scope)
                    ?: facade.findClass("java.util.$cleanType", scope)
            }

            // We can also check canNavigate() right here in the background safely!
            if (foundClass != null && foundClass.canNavigate()) {
                foundClass
            } else {
                null
            }
        }
    }
}