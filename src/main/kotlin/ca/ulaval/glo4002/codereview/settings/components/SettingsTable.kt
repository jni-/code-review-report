package ca.ulaval.glo4002.codereview.settings.components

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.table.TableModelEditor.EditableColumnInfo
import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.TableModelEvent

class SettingsTable {
    private val columns = arrayOf(SettingsColumn("Predefined rule"))
    private val tableModel = ListTableModel(columns, mutableListOf<SettingsColumnModel>())
    private val listeners = mutableListOf<(e: TableModelEvent) -> Unit>()
    val component: JBTable;

    init {
        component = JBTable(tableModel).apply {
            tableModel.addTableModelListener { e -> listeners.forEach { it(e) }}
            cellEditor = getCellEditor(this)
        }
    }

    fun addChangeListener(listener: (e: TableModelEvent) -> Unit) {
        listeners.add(listener)
    }

    fun addPredefinedRule() {
        component.cellEditor?.cancelCellEditing()
        tableModel.addRow(SettingsColumnModel(""))
        component.requestFocus()
        component.editCellAt(component.rowCount - 1, 0)
        component.changeSelection(component.rowCount - 1, 0, false, false)
    }

    fun removeSelectedRule() {
        val selectedIndex = component.selectedRow
        if (selectedIndex != -1) {
            tableModel.removeRow(selectedIndex)
        }
    }

    fun getAllRules(): List<String> {
        val rowIndexes = tableModel.rowCount - 1
        return (0..rowIndexes).map { tableModel.getItem(it).value }
    }

    fun loadRules(value: List<String>) {
        value.map(::SettingsColumnModel).forEach(tableModel::addRow)
    }

    private fun getCellEditor(table: JBTable): DefaultCellEditor = object : DefaultCellEditor(JTextField()) {
        override fun stopCellEditing(): Boolean {
            val success = super.stopCellEditing()
            if (success) {
                val row = table.selectedRow
                val column = table.selectedColumn
                if (row != -1 && column != -1) {
                    val editor = component as? JTextField
                    val value = editor?.text
                    tableModel.setValueAt(value, row, column)
                    tableModel.fireTableCellUpdated(row, column)
                }
            }
            return success
        }

        override fun cancelCellEditing() {
            super.cancelCellEditing()
            SwingUtilities.invokeLater {
                table.requestFocusInWindow()
            }
        }
    }


    private inner class SettingsColumn(name: String) : EditableColumnInfo<SettingsColumnModel, String>(name) {
        override fun valueOf(item: SettingsColumnModel?): String? {
            return item?.value
        }

        override fun setValue(item: SettingsColumnModel?, value: String?) {
            if (value != null) {
                item?.value = value
            }
        }

    }

    private data class SettingsColumnModel(var value: String)

}
