package com.mod5.ae3_abpro1_mislucas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val onEdit  : (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : ListAdapter<Transaction,
        TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView    = itemView.findViewById(R.id.tvTransactionTitle)
        private val tvDate: TextView     = itemView.findViewById(R.id.tvTransactionDateTime)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvTransactionCategory)
        private val tvAmount: TextView   = itemView.findViewById(R.id.tvTransactionAmount)
        private val ivTypeIndicator: ImageView = itemView.findViewById(R.id.ivTypeIndicator)
        private val btnEdit: ImageView   = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(transaction: Transaction) {
            tvTitle.text    = transaction.title
            tvDate.text     = "${transaction.date} - ${transaction.time}"
            tvCategory.text = transaction.category

            // Usamos formato de moneda local, facilita modificar la localización por país
            val currencyFormat =
                NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            tvAmount.text = currencyFormat.format(transaction.amount)

            // Gestiona colores e íconos basados en el tipo de transacción
            if (transaction.type == "Ingreso") {
                tvAmount.setTextColor(Color.parseColor("#00C853"))  // Verde
                ivTypeIndicator.setImageResource(android.R.drawable.arrow_up_float)
            } else {
                tvAmount.setTextColor(Color.RED)
                ivTypeIndicator.setImageResource(android.R.drawable.arrow_down_float)
            }

            // Listeners para edición y eliminación
            btnEdit.setOnClickListener   { onEdit(transaction) }
            btnDelete.setOnClickListener { onDelete(transaction) }
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}
