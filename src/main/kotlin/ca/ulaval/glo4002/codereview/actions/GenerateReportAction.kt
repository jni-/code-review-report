package ca.ulaval.glo4002.codereview.actions

import ca.ulaval.glo4002.codereview.app.infra.html.HtmlReviewGenerator
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

private const val OUTPUT_DIR = "review-html"

class GenerateReportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.service<CodeReviewService>()
        val generator = project.service<HtmlReviewGenerator>()

        ApplicationManager.getApplication().runWriteAction {
            val report = generator.generate(service.getReview())
            val projectFolder = LocalFileSystem.getInstance()
                .findFileByNioFile(Path.of(project.basePath!!))!!
            projectFolder
                .getOrCreateDir(OUTPUT_DIR)
                .findOrCreateChildData(this, "index.html")
                .setBinaryContent(report.toByteArray())
        }
    }

    private fun VirtualFile.getOrCreateDir(name: String): VirtualFile {
        return findChild(name) ?: createChildDirectory(this, name)
    }
}
