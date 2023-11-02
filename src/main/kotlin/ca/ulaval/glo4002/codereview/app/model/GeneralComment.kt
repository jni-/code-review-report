package ca.ulaval.glo4002.codereview.app.model

import java.util.UUID

data class GeneralComment(var comment: String, var isImportant: Boolean, val id: UUID = UUID.randomUUID()) {
    fun updateText(newText: String) {
        comment = newText
    }

    fun updateIsImportant(isImportant: Boolean) {
        this.isImportant = isImportant
    }
}
