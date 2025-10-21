package ca.ulaval.glo4002.codereview.actions

import ca.ulaval.glo4002.codereview.app.infra.html.HtmlReviewGenerator
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

class GenerateReportAction : AnAction() {
    private val assets: Map<String, List<String>> = mapOf(
        "css" to listOf("shCoreEclipse.css", "shThemeEclipse.css", "style.css"),
        "images" to listOf("Architecture.png", "arrow.gif", "arrow-down.gif", "arrow-top.png", "CleanCode.png", "Important.png", "Question.png", "snippet.png"),
        "js" to listOf("content.js", "content-for-reviewer.js", "jquery.min.js", "shBrushJava.js", "shBrushPlain.js", "shBrushXml.js", "shCore.js")
    )

    override fun actionPerformed(e: AnActionEvent) {
        val service = e.project?.service<CodeReviewService>()
        val generator = e.project?.service<HtmlReviewGenerator>()

        if (service == null || generator == null) {
            thisLogger().error("Could not generate report, because there is no active project")
            return
        }

        ApplicationManager.getApplication().runWriteAction {
            val report = generator.generate(service.getReview())
            getProjectFolder(e.project!!)
                .getOrCreateDir("review-html")
                .apply {
                    writeFile("index.html", report)
                }
                .getOrCreateDir("assets")
                .apply {
                    assets.forEach { (folder, assets) ->
                        assets.forEach { assetName ->
                            copyAsset(folder, assetName)
                        }
                    }
                }
        }
    }

    private fun getProjectFolder(project: Project): VirtualFile = LocalFileSystem.getInstance()
        .findFileByNioFile(Path.of(project.basePath!!))!!

    private fun VirtualFile.copyAsset(folder: String, assetName: String) {
        val empty = "".toByteArray()
        val content = GenerateReportAction::class.java.getResourceAsStream("/review-assets/$folder/$assetName")?.readBytes() ?: empty
        thisLogger().warn("Reading /review-assets/$folder/$assetName has ${content.size} bytes")
        this.getOrCreateDir(folder)
            .findOrCreateChildData(this, assetName)
            .setBinaryContent(content)
    }

    private fun VirtualFile.getOrCreateDir(name: String): VirtualFile {
        return this.findChild(name) ?: this.createChildDirectory(this, name)
    }

    private fun VirtualFile.writeFile(name: String, content: String) {
            writeFile(name, content.toByteArray())
    }

    private fun VirtualFile.writeFile(name: String, content: ByteArray) {
        this.findOrCreateChildData(this, name)
            .setBinaryContent(content)
    }
}
