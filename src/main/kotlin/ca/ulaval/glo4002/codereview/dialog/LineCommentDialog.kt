package ca.ulaval.glo4002.codereview.dialog

import ca.ulaval.glo4002.codereview.app.model.LineComment
import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import ca.ulaval.glo4002.codereview.dialog.components.PredefinedCommentsTable
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.settings.COMMENT_DIALOG_DIMENSION_SERVICE_KEY
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenTextChangedFromUi
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import org.jetbrains.plugins.template.MyBundle
import java.awt.event.*
import javax.swing.*

class LineCommentDialog(
    private val context: LineCommentContext,
    private val rules: List<String>,
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
        setSize(800, 800)
        isOKActionEnabled = false

        addKeyListener(globalKeyListener)
    }

    override fun getDimensionServiceKey(): String = COMMENT_DIALOG_DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        var textField: JBTextField? = null
        val table = PredefinedCommentsTable(MyBundle.message("dialog.lineComment.tableTitle"), rules)
        table.onCommentSelected {
            comment.set(it)
            textField?.grabFocus()
        }

        table.addKeyListener(globalKeyListener)

        return panel {
            title = MyBundle.message("dialog.lineComment.title")

            row {
                text(MyBundle.message("dialog.lineComment.subTitle", context.file.name, context.lineRange))
            }

            row {
                textField()
                    .bindText(comment)
                    .whenTextChangedFromUi {
                        table.updateFilter(it)
                        isOKActionEnabled = it.isNotEmpty()
                    }
                    .applyToComponent {
                        textField = this
                        addKeyListener(object : KeyAdapter() {
                            override fun keyReleased(e: KeyEvent) {
                                if (e.keyCode == KeyEvent.VK_DOWN || e.keyCode == KeyEvent.VK_UP) {
                                    table.moveToRow(if(e.keyCode == KeyEvent.VK_DOWN) 1 else -1)
                                }
                            }
                        })
                        addKeyListener(globalKeyListener)
                    }
                    .focused()
                    .resizableColumn()
                    .horizontalAlign(HorizontalAlign.FILL)
            }

            row {
                scrollCell(table)
                    .resizableColumn()
                    .horizontalAlign(HorizontalAlign.FILL)
                    .verticalAlign(VerticalAlign.FILL)
            }.resizableRow()

            row {
                checkBox(MyBundle.message("dialog.lineComment.important"))
                    .bindSelected(important)
            }

        }
    }

    override fun doOKAction() {
        if (this.isOKActionEnabled) {
            service.addComment(LineComment(comment.get(), important.get(), context))
        }

        super.doOKAction()
    }
}
