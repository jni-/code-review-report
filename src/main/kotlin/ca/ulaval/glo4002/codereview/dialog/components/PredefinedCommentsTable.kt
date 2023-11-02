package ca.ulaval.glo4002.codereview.dialog.components

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.RowFilter
import javax.swing.table.TableRowSorter

private const val ENTER_ACTION_KEY = "SetComment"

class PredefinedCommentsTable(
    title: String,
    predefinedComments: List<String>,
    private val tableModel: ListTableModel<PredefinedRuleColumnModel> = ListTableModel(
        arrayOf(
            PredefinedRuleColumnInfo(
                title
            )
        ), predefinedComments.map(::PredefinedRuleColumnModel)
    )
) : JBTable(tableModel) {
    private val commentListeners: MutableList<(comment: String) -> Unit> = mutableListOf()
    private val filter: FuzzyFilter = FuzzyFilter()

    init {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        columnSelectionAllowed = true
        cellSelectionEnabled = true

        val tableSorter = TableRowSorter(tableModel)
        tableSorter.rowFilter = filter
        rowSorter = tableSorter

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) fireCommentSelected()
            }
        })

        val enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, ENTER_ACTION_KEY)

        actionMap.put(ENTER_ACTION_KEY, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) = fireCommentSelected()
        })
    }

    private fun fireCommentSelected() {
        if (selectedRow != -1) {
            commentListeners.forEach { it(getValueAt(selectedRow, 0) as String) }
        }
    }

    fun updateFilter(filterValue: String) {
        filter.updateFilter(filterValue) {
            tableModel.fireTableDataChanged()
        }
    }

    fun onCommentSelected(callback: (comment: String) -> Unit) {
        commentListeners.add(callback)
    }

    fun moveToRow(increment: Int) {
        val newRow = selectedRow + increment
        val lastRow = rowCount - 1
        if (newRow in 0..lastRow) {
            changeSelection(newRow, 0, false, false)
            grabFocus()
        }
    }
}

class FuzzyFilter : RowFilter<ListTableModel<PredefinedRuleColumnModel>, Int>() {
    private var filter: String = ""

    override fun include(entry: Entry<out ListTableModel<PredefinedRuleColumnModel>, out Int>): Boolean {
        return if (filter.isNotEmpty()) {
            val value = entry.model.getItem(entry.identifier).value
            filter.split(" ").all { value.lowercase().contains(it.lowercase()) }
        } else true
    }

    fun updateFilter(newFilter: String, onUpdate: () -> Unit) {
        if (filter != newFilter) {
            filter = newFilter
            onUpdate()
        }
    }
}

class PredefinedRuleColumnInfo(name: String) : ColumnInfo<PredefinedRuleColumnModel, String>(name) {
    override fun valueOf(item: PredefinedRuleColumnModel?): String? {
        return item?.value
    }
}

data class PredefinedRuleColumnModel(var value: String)
