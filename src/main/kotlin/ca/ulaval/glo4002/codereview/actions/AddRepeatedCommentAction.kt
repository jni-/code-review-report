package ca.ulaval.glo4002.codereview.actions

import ca.ulaval.glo4002.codereview.app.infra.code.LineContextExtractor
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.dialog.RepeatedCommentDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class AddRepeatedCommentAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        LineContextExtractor.extractFromCursor(project)?.let {
            RepeatedCommentDialog(it, project!!.service<CodeReviewService>()).show()
        }
    }
}
