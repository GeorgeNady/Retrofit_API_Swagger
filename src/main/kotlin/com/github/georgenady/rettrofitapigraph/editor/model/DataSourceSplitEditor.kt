package com.github.georgenady.rettrofitapigraph.editor.model

import com.github.georgenady.rettrofitapigraph.editor.DataSourceDesignEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class DataSourceSplitEditor(
    textEditor: TextEditor,
    designEditor: DataSourceDesignEditor
) : TextEditorWithPreview(
    myEditor = textEditor,
    myPreview = designEditor,
    name = "Api Swagger",
    layout = Layout.SHOW_EDITOR_AND_PREVIEW,
    defaultLayout = Layout.SHOW_EDITOR_AND_PREVIEW,
    isVerticalSplit = true,
)