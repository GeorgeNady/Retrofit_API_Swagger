package com.github.georgenady.rettrofitapigraph.editor

import com.github.georgenady.rettrofitapigraph.MyBundle
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class RetrofitSplitEditor(
    textEditor: TextEditor,
    designEditor: RetrofitDesignEditor
) : TextEditorWithPreview(
    textEditor,
    designEditor,
    MyBundle.message("split.editor.title"),
    Layout.SHOW_EDITOR_AND_PREVIEW
)