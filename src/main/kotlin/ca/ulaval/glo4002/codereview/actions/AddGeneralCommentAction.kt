package ca.ulaval.glo4002.codereview.actions

import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.dialog.GeneralCommentDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class AddGeneralCommentAction : AnAction(
    "Add General Comment"
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            GeneralCommentDialog(project.service<CodeReviewService>()).show()
        }
    }
}
