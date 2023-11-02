package ca.ulaval.glo4002.codereview.toolWindow

import ca.ulaval.glo4002.codereview.app.infra.html.HtmlReviewGenerator
import ca.ulaval.glo4002.codereview.app.model.LineCommentContextIdentifier
import ca.ulaval.glo4002.codereview.app.model.Review
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.bus.CommentSelectedListener
import ca.ulaval.glo4002.codereview.bus.ReviewChangedListener
import ca.ulaval.glo4002.codereview.dialog.EditCommentDialog
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.addKeyboardAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.htmlComponent
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.runBlocking
import java.awt.GridLayout
import java.awt.event.InputEvent
import java.nio.file.Path
import java.util.*
import javax.swing.JButton
import javax.swing.KeyStroke
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener


class CodeReviewPreviewWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {
        companion object {
            const val ID = "CodeReviewToolWindow"
        }

        private val service = toolWindow.project.service<CodeReviewService>()
        private val generator = toolWindow.project.service<HtmlReviewGenerator>()

        fun getContent() = JBPanel<JBPanel<*>>(GridLayout()).apply {
            val bus = toolWindow.project.messageBus.connect()
            bus.subscribe(ReviewChangedListener.TOPIC, object : ReviewChangedListener {
                override fun onReviewChanged(review: Review) {
                    printCodeReview(review, null)
                    revalidate()
                    updateUI()
                }
            })

            bus.subscribe(CommentSelectedListener.TOPIC, object : CommentSelectedListener {
                override fun onCommentSelected(review: Review, selectedComment: UUID) {
                    printCodeReview(review, selectedComment)
                    revalidate()
                    updateUI()
                }
            })

            printCodeReview(service.getReview(), null)
        }


        private fun JBPanel<JBPanel<*>>.printCodeReview(review: Review, selectedComment: UUID?) = runBlocking {
            removeAll()
            val listener = Listener(service)
            val rawHtml = generator.generateForTools(review, selectedComment)
            val html = htmlComponent(rawHtml, hyperlinkListener = listener)
            add(JBScrollPane(html))
        }

        class Listener(val service: CodeReviewService) : HyperlinkListener {
            override fun hyperlinkUpdate(e: HyperlinkEvent?) {
                if (e?.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                    val project = this@Listener.service.project
                    val instance = FileEditorManager.getInstance(project)
                    val url = e?.description
                    if (url != null && url.contains(":")) {
                        val command = url.split(":")[0]
                        val args = url.split(":")[1]
                        when (command) {
                            "repeated" -> {
                                service.makeCommentRepeated(UUID.fromString(args))
                            }

                            "unrepeated" -> {
                                service.makeCommentUnrepeated(UUID.fromString(args))
                            }

                            "unrepeatedOnce" -> {
                                val split = args.split("#")
                                val id = split[0]
                                val identifier = LineCommentContextIdentifier.fromString(split[1])
                                service.makeExampleUnrepeated(UUID.fromString(id), identifier)
                            }

                            "remove" -> {
                                service.removeCommentById(UUID.fromString(args))
                            }

                            "removeExample" -> {
                                val split = args.split("#")
                                val id = split[0]
                                val identifier = LineCommentContextIdentifier.fromString(split[1])
                                service.removeExample(UUID.fromString(id), identifier)
                            }

                            "edit" -> {
                                val id = UUID.fromString(args)
                                val comment = service.getCommentText(id)
                                EditCommentDialog(id, comment, service).show()
                            }

                            "goto" -> {
                                val filePath = args.split("#")[0]
                                val line = if (args.contains("#")) url.split("#")[1].toInt() - 1 else 1
                                val file = LocalFileSystem.getInstance().findFileByNioFile(Path.of(filePath))
                                if (file != null) {
                                    instance.openFileEditor(OpenFileDescriptor(project, file, line, 1), true)
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
