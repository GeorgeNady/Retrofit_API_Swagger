package com.github.georgenady.retrofitApiSwagger.editor.model

import com.github.georgenady.retrofitApiSwagger.editor.DataSourceDesignEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class DataSourceSplitEditor(
    textEditor: TextEditor,
    designEditor: DataSourceDesignEditor
) : TextEditorWithPreview(
    myEditor = textEditor,
    myPreview = designEditor,
    name = "Ktorfit & Retrofit Studio",
    defaultLayout = Layout.SHOW_EDITOR_AND_PREVIEW,
    isVerticalSplit = false,
)