package com.mod5.ae3_abpro1_mislucas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round

// ViewModel para gestionar las transacciones atómicas y el balance (saldo).
class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)

    // LiveData principal que contiene todas las transacciones (la lista).
    private val _allTransactions = MutableLiveData<List<Transaction>>()
    val allTransactions: LiveData<List<Transaction>> get() = _allTransactions

    // LiveData para notificar el nuevo balance (Ingresos, Gastos, Saldo).
    private val _balanceData = MutableLiveData<BalanceData>()
    val balanceData: LiveData<BalanceData> get() = _balanceData

    // LiveData para notificar mensajes de estado a la interfaz (Toast).
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> get() = _statusMessage

    init {
        loadTransactions() // Carga inicial al crear el ViewModel
    }

    // Carga las transacciones, actualiza la lista y recalcula el balance de forma reactiva.
    fun loadTransactions() {
        // Ejecuta la carga de datos en un hilo de fondo (IO)
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.readAllTransactions()
            _allTransactions.postValue(transactions) // Actualiza la lista
            calculateBalance(transactions) // Recalcula el saldo
        }
    }

    // Calcula los totales de Ingresos, Gastos y Saldo.
    private fun calculateBalance(transactions: List<Transaction>) {
        val totalIncome  = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
        val balance      = totalIncome - totalExpense

        // manejamos montos con decimales para que sea multimoneda, redondea a dos decimales
        val roundedBalance = BalanceData(
            totalIncome    = round(totalIncome * 100) / 100,
            totalExpense   = round(totalExpense * 100) / 100,
            balance        = round(balance * 100) / 100
        )

        _balanceData.postValue(roundedBalance)
    }

    // Guarda o actualiza una transacción. Después, recarga la lista para actualizar la interfaz.
    fun saveOrUpdateTransaction(
        id         : String?,
        title      : String,
        description: String,
        type       : String,
        category   : String,
        amount     : Double,
        date       : String,
        time       : String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val isEditing     = id != null
            val transactionId = id ?: UUID.randomUUID().toString()
            val transaction   = Transaction(transactionId, title, description, type, category,
                                            amount, date, time)

            val success = if (isEditing) {
                repository.updateTransactionInCSV(transaction)
            } else {
                repository.saveTransactionToCSV(transaction)
            }

            if (success) {
                _statusMessage.postValue("Transacción ${if (isEditing) "actualizada"
                else "guardada"} correctamente")
                loadTransactions() // Recarga y actualiza el LiveData y el Balance.
            } else {
                _statusMessage.postValue("Error al guardar la transacción")
            }
        }
    }

    // Elimina una transacción por ID y recarga el estado.
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.deleteTransactionById(transaction.id)
            if (success) {
                _statusMessage.postValue("Transacción eliminada")
                loadTransactions() // Recarga la lista y el balance.
            } else {
                _statusMessage.postValue("Error al eliminar la transacción")
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}