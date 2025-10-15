package com.mod5.ae3_abpro1_mislucas

import java.lang.Double.parseDouble

// Modelo de datos para Gastos e Ingresos.
data class Transaction(
    val id: String,
    val title: String,
    val description: String,
    val type: String,      // Ingreso o Gasto
    val category: String,  // Ocio, Supermercado, Transporte, Hogar, Trabajo, Otros
    val amount: Double,
    val date: String,      // DD/MM/AAAA
    val time: String       // HH:MM
) {
    // Convierte el objeto a una línea de CSV
    fun toCsvString(): String {
        return "$id,$title,$description,$type,$category,$amount,$date,$time"
    }

    companion object {
        // Cada operación de ingreso/gasto, se registra como una línea en el CSV
        // Esta función crea un objeto 'Transaction' a partir de una línea de CSV
        fun fromCsvString(csvString: String): Transaction? {
            return try {
                val parts = csvString.split(',')
                if (parts.size != 8) return null
                Transaction(
                    id          = parts[0],
                    title       = parts[1],
                    description = parts[2],
                    type        = parts[3],
                    category    = parts[4],
                    amount      = parseDouble(parts[5]), // chequeamos el parseo del Double
                    date        = parts[6],
                    time        = parts[7]
                )
            } catch (e: Exception) {
                println("Error parsing CSV line: $csvString - ${e.message}")
                null
            }
        }
    }
}

// Manejamos la conversión de String a Double, asegurando que se use el punto (.) como
// separador decimal estándar, independiente de la configuración regional del dispositivo.
// Permite que la app pueda usarse como multimoneda o servir para criptos (por el fraccionamiento)
fun parseDouble(value: String): Double {
    return value.replace(',', '.').toDouble()
}

// Contenedor de LiveData para poder mostrar los subtotales y el saldo.
data class BalanceData(
    val totalIncome : Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance     : Double = 0.0
)