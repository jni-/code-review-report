package ca.ulaval.glo4002.codereview.dialog.components

import ca.ulaval.glo4002.codereview.app.model.RepeatedComment
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.AbstractAction
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.RowFilter
import javax.swing.table.TableRowSorter

private const val ENTER_ACTION_KEY = "SetComment"

class RepeatedCommentsTable(
    title: String,
    comments: List<RepeatedComment>,
    private val tableModel: ListTableModel<RepeatedCommentTableModel> = ListTableModel(
        arrayOf(
            RepeatedCommentColumnInfo(
                title
            )
        ), comments.map { RepeatedCommentTableModel(it.id, it.comment) }
    )
) : JBTable(tableModel) {
    private val commentListeners: MutableList<(id: UUID, comment: String, selected: Boolean) -> Unit> = mutableListOf()
    private val filter: CommentFuzzyFilter = CommentFuzzyFilter()

    init {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        columnSelectionAllowed = true
        cellSelectionEnabled = true

        val tableSorter = TableRowSorter(tableModel)
        tableSorter.rowFilter = filter
        rowSorter = tableSorter

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 1) fireCommentHighlighted()
                if (e.clickCount == 2) fireCommentSelected()
            }
        })

        val enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, ENTER_ACTION_KEY)

        actionMap.put(ENTER_ACTION_KEY, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) = fireCommentSelected()
        })
    }

    private fun fireCommentHighlighted() {
        if (selectedRow != -1) {
            commentListeners.forEach {
                val selection = tableModel.getItem(this.convertRowIndexToModel(selectedRow))
                it(selection.id, selection.value, false)
            }
        }
    }

    private fun fireCommentSelected() {
        if (selectedRow != -1) {
            commentListeners.forEach {
                val selection = tableModel.getItem(this.convertRowIndexToModel(selectedRow))
                it(selection.id, selection.value, true)
            }
        }
    }

    fun updateFilter(filterValue: String) {
        filter.updateFilter(filterValue) {
            tableModel.fireTableDataChanged()
        }
    }

    fun onCommentSelected(callback: (id: UUID, comment: String, selected: Boolean) -> Unit) {
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

class CommentFuzzyFilter : RowFilter<ListTableModel<RepeatedCommentTableModel>, Int>() {
    private var filter: String = ""

    override fun include(entry: Entry<out ListTableModel<RepeatedCommentTableModel>, out Int>): Boolean {
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

class RepeatedCommentColumnInfo(name: String) : ColumnInfo<RepeatedCommentTableModel, String>(name) {
    override fun valueOf(item: RepeatedCommentTableModel?): String? {
        return item?.value
    }
}

data class RepeatedCommentTableModel(val id: UUID, val value: String)
