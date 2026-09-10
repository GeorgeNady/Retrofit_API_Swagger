package com.github.georgenady.retrofitApiSwagger.scanner.cache.impl

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.github.georgenady.retrofitApiSwagger.scanner.cache.EndpointScanCache
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe project-level implementation of [com.github.georgenady.retrofitApiSwagger.scanner.cache.EndpointScanCache] tracking [com.intellij.openapi.vfs.VirtualFile.getModificationStamp].
 */
@Service(Service.Level.PROJECT)
internal class EndpointScanCacheImpl(
    private val project: Project
) : EndpointScanCache {

    private data class CacheEntry(
        val modificationStamp: Long,
        val endpoints: List<ApiNode>
    )

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun get(file: VirtualFile): List<ApiNode>? {
        if (!file.isValid) return null
        val entry = cache[file.path] ?: return null
        return if (entry.modificationStamp == file.modificationStamp) {
            entry.endpoints
        } else {
            null
        }
    }

    override fun put(file: VirtualFile, endpoints: List<ApiNode>) {
        if (!file.isValid) return
        cache[file.path] = CacheEntry(file.modificationStamp, endpoints)
    }

    override fun invalidate(file: VirtualFile) {
        cache.remove(file.path)
    }

    override fun invalidatePath(path: String) {
        cache.remove(path)
    }

    override fun getAllCached(): List<ApiNode> {
        return cache.values.flatMap { it.endpoints }
    }

    override fun clear() {
        cache.clear()
    }
}
