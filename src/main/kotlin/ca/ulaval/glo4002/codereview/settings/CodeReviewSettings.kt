package ca.ulaval.glo4002.codereview.settings

import ca.ulaval.glo4002.codereview.settings.components.SettingsTable
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class CodeReviewSettings : Configurable {
    private var modified = false
    private val table = SettingsTable()

    override fun createComponent(): JComponent {
        var scrollToBottom = false
        var lastScrollMaximum = 0
        val scrollPane = JBScrollPane(table.component)
        table.addChangeListener { this.modified = true }

        scrollPane.verticalScrollBar.addAdjustmentListener { e ->
            if (scrollToBottom && e.adjustable.maximum > lastScrollMaximum) {
                e.adjustable.value = e.adjustable.maximum
                scrollToBottom = false
                lastScrollMaximum = e.adjustable.maximum
            }
        }

        return panel {
            row {
                cell(scrollPane)
                    .resizableColumn()
                    .align(Align.FILL)
            }.resizableRow()

            row {
                button("Add") {
                    table.addPredefinedRule()
                    scrollToBottom = true
                }
                button("Remove") {
                    table.removeSelectedRule()
                }
            }
        }
    }

    override fun isModified(): Boolean {
        return modified
    }

    override fun apply() {
        val settings = SettingsPersistence.getInstance()
        settings.updateRules(table.getAllRules())
        this.modified = false;
    }

    override fun reset() {
        val settings = SettingsPersistence.getInstance()
        table.loadRules(settings.state.predefinedRules)
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Code Review"
    }
}
