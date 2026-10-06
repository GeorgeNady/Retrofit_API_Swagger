package com.github.georgenady.retrofitApiSwagger.scanner.listener

import com.github.georgenady.retrofitApiSwagger.scanner.cache.EndpointScanCache
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

/**
 * VFS listener that observes file modifications and invalidates cached endpoints reactively.
 */
internal class RetrofitFileChangeListener(
    private val project: Project
) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        val cache = project.getService(EndpointScanCache::class.java) ?: return

        for (event in events) {
            val path = event.path
            if (!path.endsWith(".kt") && !path.endsWith(".java")) {
                continue
            }

            when (event) {
                is VFileContentChangeEvent,
                is VFileDeleteEvent,
                is VFilePropertyChangeEvent -> {
                    cache.invalidatePath(path)
                }
            }
        }
    }
}
