package com.leshen.letseatmobile.restaurantList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.leshen.letseatmobile.R

class TablesAdapter(
    private val tables: List<Table>,
    private val onTableClick: (Table) -> Unit
) : RecyclerView.Adapter<TablesAdapter.TableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.table_item, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val table = tables[position]
        holder.buttonTable.text = "Stolik nr ${table.tableId} (${table.size} os.)"
        holder.buttonTable.setOnClickListener {
            onTableClick(table)
        }
    }

    override fun getItemCount(): Int {
        return tables.size
    }

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonTable: TextView = itemView.findViewById(R.id.buttonTable)
    }
}