package ca.ulaval.glo4002.codereview.dialog

import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.dialog.components.RepeatedCommentsTable
import ca.ulaval.glo4002.codereview.settings.REPEATED_COMMENT_DIALOG_DIMENSION_SERVICE_KEY
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenTextChangedFromUi
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import org.jetbrains.plugins.template.MyBundle
import java.awt.event.*
import java.util.*
import javax.swing.*

class RepeatedCommentDialog(
    private val context: LineCommentContext,
    private val service: CodeReviewService
) : DialogWrapper(
    true
) {
    private var selectedCommentId: UUID? = null
    private var selectedCommentText: String? = null

    init {
        init()
        setSize(800, 800)
        isOKActionEnabled = false
    }

    override fun getDimensionServiceKey(): String = REPEATED_COMMENT_DIALOG_DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        var infoText: JLabel? = null
        val table = RepeatedCommentsTable(MyBundle.message("dialog.lineComment.tableTitle"), service.getReview().repeatedComments)
        table.onCommentSelected { id, comment, selected ->
            selectedCommentId = id
            selectedCommentText = comment
            isOKActionEnabled = true
            infoText?.text = "Adding for comment: $comment"

            if (selected) {
                doOKAction()
            }
        }

        return panel {
            title = MyBundle.message("dialog.repeated.title")

            row {
                text(MyBundle.message("dialog.repeated.subTitle", context.file.name, context.lineRange))
            }
            row {
                textField()
                    .whenTextChangedFromUi {
                        selectedCommentId = null
                        selectedCommentText = null
                        isOKActionEnabled = false
                        table.updateFilter(it)
                    }
                    .applyToComponent {
                        addKeyListener(object : KeyAdapter() {
                            override fun keyReleased(e: KeyEvent) {
                                if (e.keyCode == KeyEvent.VK_DOWN || e.keyCode == KeyEvent.VK_UP) {
                                    table.moveToRow(if(e.keyCode == KeyEvent.VK_DOWN) 1 else -1)
                                }
                            }
                        })
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
                label("").applyToComponent { infoText = this }
            }
        }
    }

    override fun doOKAction() {
        if (this.isOKActionEnabled && selectedCommentId != null) {
            service.addExampleComment(selectedCommentId!!, context)
        }

        super.doOKAction()
    }
}
