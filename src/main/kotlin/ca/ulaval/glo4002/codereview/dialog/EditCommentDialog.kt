package ca.ulaval.glo4002.codereview.dialog

import ca.ulaval.glo4002.codereview.app.model.EditableComment
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.settings.EDIT_COMMENT_DIALOG_DIMENSION_SERVICE_KEY
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenTextChangedFromUi
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import org.jetbrains.plugins.template.MyBundle
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.JComponent

class EditCommentDialog(
    private val id: UUID,
    private val initialComment: EditableComment,
    private val service: CodeReviewService
) : DialogWrapper(
    true
) {
    private val comment: AtomicLazyProperty<String> = AtomicLazyProperty { initialComment.text }
    private val important: AtomicLazyProperty<Boolean> = AtomicLazyProperty { initialComment.isImportant }
    private val globalKeyListener = object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_I && e.isControlDown) {
                important.updateAndGet { !it }
            }
        }
    }

    init {
        init()
        setSize(400, 400)
        isOKActionEnabled = initialComment.text.isNotBlank()

        addKeyListener(globalKeyListener)
    }

    override fun getDimensionServiceKey(): String = EDIT_COMMENT_DIALOG_DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        return panel {
            title = MyBundle.message("dialog.edit.title")

            row {
                text(MyBundle.message("dialog.edit.subTitle"))
            }

            row {
                textArea()
                    .bindText(comment)
                    .whenTextChangedFromUi {
                        isOKActionEnabled = it.isNotBlank()
                    }
                    .applyToComponent {
                        rows = 10

                        addKeyListener(globalKeyListener)
                    }
                    .focused()
                    .resizableColumn()
                    .horizontalAlign(HorizontalAlign.FILL)
            }

            row {
                checkBox(MyBundle.message("dialog.edit.important"))
                    .bindSelected(important)
            }
        }
    }

    override fun doOKAction() {
        if (this.isOKActionEnabled) {
            service.editCommentText(id, comment.get(), important.get())
        }

        super.doOKAction()
    }
}
