package com.mod5.ae3_abpro1_mislucas

import android.app.Application
import java.io.*
import java.util.UUID

class TransactionRepository(application: Application) {

    private val context  = application
    private val fileName = "transacciones.csv"
    private val file     = File(context.getExternalFilesDir(null), fileName)

    init {
        // Asegura que el archivo exista
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    // Carga todas las transacciones del archivo CSV y las ordena por fecha y hora (más reciente primero).
    fun readAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        if (!file.exists()) return transactions

        try {
            BufferedReader(FileReader(file)).use { reader ->
                reader.forEachLine { line ->
                    Transaction.fromCsvString(line)?.let { transactions.add(it) }
                }
            }
        } catch (e: Exception) {
            println("Error al leer el archivo CSV: ${e.message}")
        }
        // Ordena por fecha y hora de forma descendente (más reciente arriba)
        return transactions.sortedWith(compareByDescending<Transaction> { it.date }
            .thenByDescending { it.time })
    }

    // Guarda la lista completa de transacciones en el CSV.
    private fun saveAllTransactions(transactions: List<Transaction>): Boolean {
        return try {
            BufferedWriter(FileWriter(file)).use { writer ->
                transactions.forEach { transaction ->
                    writer.write(transaction.toCsvString())
                    writer.newLine()
                }
            }
            true
        } catch (e: Exception) {
            println("Error al escribir en el archivo CSV: ${e.message}")
            false
        }
    }

    // Guarda una nueva transacción.
    fun saveTransactionToCSV(transaction: Transaction): Boolean {
        val transactions = readAllTransactions().toMutableList()
        transactions.add(transaction)
        return saveAllTransactions(transactions)
    }

    // Actualiza una transacción existente.
    fun updateTransactionInCSV(updatedTransaction: Transaction): Boolean {
        val transactions = readAllTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }

        return if (index != -1) {
            transactions[index] = updatedTransaction
            saveAllTransactions(transactions)
        } else {
            // Si no se encuentra, se considera como una nueva
            saveTransactionToCSV(updatedTransaction)
        }
    }

    // Elimina una transacción por ID.
    fun deleteTransactionById(id: String): Boolean {
        val transactions = readAllTransactions().toMutableList()
        val originalSize = transactions.size
        transactions.removeIf { it.id == id }

        return if (transactions.size < originalSize) {
            saveAllTransactions(transactions)
        } else {
            false
        }
    }
}