package com.github.georgenady.rettrofitapigraph.editor

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class RetrofitSplitEditor(
    textEditor: TextEditor,
    designEditor: RetrofitDesignEditor
) : TextEditorWithPreview(
    textEditor,
    designEditor,
    "RetrofitApiGraph",
    Layout.SHOW_EDITOR_AND_PREVIEW
)