package com.mod5.ae3_abpro1_mislucas

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ListaTransaccionesFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    // Elementos de Balance
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.lista_transacciones, container, false)
        // Inicializa el ViewModel
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)

        // Inicializar vistas de balance
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        tvBalance = view.findViewById(R.id.tvBalance)

        // Configurar RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewTransactions)
        adapter = TransactionAdapter(
            onEdit = { transaction -> navigateToEdit(transaction) },
            onDelete = { transaction -> transactionViewModel.deleteTransaction(transaction) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configurar Observadores (Patrón Observador con LiveData)
        setupObservers()

        return view
    }

    /**
     * Configura los observadores para LiveData.
     */
    private fun setupObservers() {
        // Observa la lista de transacciones (Actualiza el RecyclerView)
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactions?.let {
                adapter.submitList(it)
            }
        }

        // Observa los datos del balance (Actualiza los subtotales y saldo)
        // CRUCIAL para la reactividad de los totales
        transactionViewModel.balanceData.observe(viewLifecycleOwner) { balanceData ->
            balanceData?.let {
                updateBalanceUI(it.totalIncome, it.totalExpense, it.balance)
            }
        }

        // Observa mensajes de estado (Muestra Toasts)
        transactionViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                transactionViewModel.clearStatusMessage()
            }
        }
    }

    /**
     * Actualiza la interfaz de usuario con los totales y el saldo, incluyendo el color condicional.
     */
    private fun updateBalanceUI(income: Double, expense: Double, balance: Double) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        tvTotalIncome.text = "Ingresos: ${currencyFormat.format(income)}"
        tvTotalExpense.text = "Gastos: ${currencyFormat.format(expense)}"
        tvBalance.text = "SALDO: ${currencyFormat.format(balance)}"

        // Lógica de color condicional: Rojo para negativo, verde/teal para positivo
        if (balance < 0) {
            tvBalance.setTextColor(Color.RED)
        } else {
            tvBalance.setTextColor(Color.parseColor("#00796B"))
        }
    }

    private fun navigateToEdit(transaction: Transaction) {
        (activity as? MainActivity)?.loadFragment(RegistroTransaccionFragment.newInstance(transaction))
    }

    companion object {
        fun newInstance() = ListaTransaccionesFragment()
    }
}
