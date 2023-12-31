package ca.ulaval.glo4002.codereview.actions

import ca.ulaval.glo4002.codereview.app.infra.code.LineContextExtractor
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.dialog.LineCommentDialog
import ca.ulaval.glo4002.codereview.settings.SettingsPersistence
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class AddLineCommentAction : AnAction(
    "Add Line Comment"
) {
    companion object {
        private var settings: SettingsPersistence = SettingsPersistence.getInstance()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        LineContextExtractor.extractFromCursor(project)?.let {
            LineCommentDialog(it, settings.getRules(), project!!.service<CodeReviewService>()).show()
        }
    }
}
