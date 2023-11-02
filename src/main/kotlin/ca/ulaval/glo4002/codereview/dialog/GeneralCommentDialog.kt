package ca.ulaval.glo4002.codereview.dialog

import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.settings.GENERAL_COMMENT_DIALOG_DIMENSION_SERVICE_KEY
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
import javax.swing.*

class GeneralCommentDialog(
    private val service: CodeReviewService
) : DialogWrapper(
    true
) {
    private val comment: AtomicLazyProperty<String> = AtomicLazyProperty { "" }
    private val important: AtomicLazyProperty<Boolean> = AtomicLazyProperty { false }
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
        isOKActionEnabled = false

        addKeyListener(globalKeyListener)
    }

    override fun getDimensionServiceKey(): String = GENERAL_COMMENT_DIALOG_DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        return panel {
            title = MyBundle.message("dialog.general.title")

            row {
                text(MyBundle.message("dialog.general.subTitle"))
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
                checkBox(MyBundle.message("dialog.general.important"))
                    .bindSelected(important)
            }
        }
    }

    override fun doOKAction() {
        if (this.isOKActionEnabled) {
            service.addGeneralComment(comment.get(), important.get())
        }

        super.doOKAction()
    }
}
