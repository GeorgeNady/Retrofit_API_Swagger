package com.github.georgenady.retrofitApiSwagger.scanner.cache

import com.github.georgenady.retrofitApiSwagger.model.ApiNode
import com.intellij.openapi.vfs.VirtualFile

/**
 * Cache contract for storing and retrieving parsed Retrofit [com.github.georgenady.retrofitApiSwagger.model.ApiNode] endpoints
 * indexed by [com.intellij.openapi.vfs.VirtualFile] and modification stamp.
 */
interface EndpointScanCache {

    /**
     * Retrieves cached endpoints for [file] if its current modification stamp matches the cached stamp.
     * Returns `null` if the file is not cached or if it has been modified since it was cached.
     */
    fun get(file: VirtualFile): List<ApiNode>?

    /**
     * Stores parsed [endpoints] for [file] capturing its current modification stamp.
     */
    fun put(file: VirtualFile, endpoints: List<ApiNode>)

    /**
     * Invalidates any cached entry for [file].
     */
    fun invalidate(file: VirtualFile)

    /**
     * Invalidates any cached entry matching [path].
     */
    fun invalidatePath(path: String)

    /**
     * Returns a flattened list of all valid cached endpoints across all files.
     */
    fun getAllCached(): List<ApiNode>

    /**
     * Clears all cached entries.
     */
    fun clear()
}
