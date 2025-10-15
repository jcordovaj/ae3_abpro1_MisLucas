package com.mod5.ae3_abpro1_mislucas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.*

class RegistroTransaccionFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel

    // Vistas
    private lateinit var editTextTitle      : EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextAmount     : EditText
    private lateinit var editTextDate       : EditText
    private lateinit var editTextTime       : EditText
    private lateinit var spinnerType        : Spinner
    private lateinit var spinnerCategory    : Spinner
    private lateinit var buttonSave         : Button

    // Variables de Estado
    private var transactionId: String? = null
    private var selectedDate : String = ""
    private var selectedTime : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.registro_transaccion,
            container,
            false)

        transactionViewModel =
            ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)

        // Inicialización de Vistas
        editTextTitle        = view.findViewById(R.id.editTextTransactionTitle)
        editTextDescription  = view.findViewById(R.id.editTextTransactionDescription)
        editTextAmount       = view.findViewById(R.id.editTextTransactionAmount)
        editTextDate         = view.findViewById(R.id.editTextTransactionDate)
        editTextTime         = view.findViewById(R.id.editTextTransactionTime)
        spinnerType          = view.findViewById(R.id.spinnerTransactionType)
        spinnerCategory      = view.findViewById(R.id.spinnerTransactionCategory)
        buttonSave           = view.findViewById(R.id.buttonSaveTransaction)

        // Setup de Pickers
        editTextDate.setOnClickListener { showDatePickerDialog() }
        editTextTime.setOnClickListener { showTimePickerDialog() }

        // Cargar datos si es edición
        arguments?.let { args ->
            transactionId = args.getString(TRANSACTION_ID_KEY)
            editTextTitle.setText(args.getString(TRANSACTION_TITLE_KEY))
            editTextDescription.setText(args.getString(TRANSACTION_DESCRIPTION_KEY))
            // Usamos un simple String.valueOf() para el Double
            editTextAmount.setText(args.getDouble(TRANSACTION_AMOUNT_KEY).toString())
            selectedDate = args.getString(TRANSACTION_DATE_KEY) ?: ""
            selectedTime = args.getString(TRANSACTION_TIME_KEY) ?: ""
            editTextDate.setText(selectedDate)
            editTextTime.setText(selectedTime)

            // Configurar Spinners
            (spinnerType.adapter as? ArrayAdapter<String>)?.
            getPosition(args.getString(TRANSACTION_TYPE_KEY))?.
            let { spinnerType.setSelection(it) }
            (spinnerCategory.adapter as? ArrayAdapter<String>)?.
            getPosition(args.getString(TRANSACTION_CATEGORY_KEY))?.
            let { spinnerCategory.setSelection(it) }

            buttonSave.text = "Actualizar Transacción"
        } ?: run {
            buttonSave.text = "Guardar Transacción"
        }
        // Listener botón grabar
        buttonSave.setOnClickListener {
            saveTransaction()
        }
        return view
    }

    // Valida y guarda la transacción.
    private fun saveTransaction() {
        val title       = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val amountText  = editTextAmount.text.toString().trim()
        val type        = spinnerType.selectedItem.toString()
        val category    = spinnerCategory.selectedItem.toString()

        // Valida campos obligatorios
        if (title.isEmpty() ||
            amountText.isEmpty() ||
            selectedDate.isEmpty() ||
            selectedTime.isEmpty()) {
            Toast.makeText(requireContext(),
                "Título, Monto, Fecha y Hora son obligatorios.",
                Toast.LENGTH_LONG).show()
            return
        }

        val amount: Double
        try {
            amount = amountText.toDouble()
            if (amount <= 0) {
                Toast.makeText(requireContext(), "El monto debe ser positivo.",
                    Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Monto inválido. Pruebe formato de" +
                    " números con punto decimal (ej: 5000.50).", Toast.LENGTH_LONG).show()
            return
        }

        // Llama al ViewModel para guardar/actualizar
        transactionViewModel.saveOrUpdateTransaction(
            id          = transactionId,
            title       = title,
            description = description,
            type        = type,
            category    = category,
            amount      = amount,
            date        = selectedDate,
            time        = selectedTime
        )

        // recupera el foco en el main y limpia
        (activity as? MainActivity)?.loadFragment(ListaTransaccionesFragment.newInstance())
        resetFormFields()
    }

    private fun showDatePickerDialog() {
        // Implementa el Date Picker
        val calendar   = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                updateDateTimeFields(selectedYear,
                    selectedMonth, selectedDay, -1, -1)
            }, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        // Implementa el Time Picker
        val calendar   = Calendar.getInstance()
        val timePicker = TimePickerDialog(requireContext(),
            { _, selectedHour, selectedMinute ->
                updateDateTimeFields(-1, -1,
                    -1, selectedHour, selectedMinute)
            }, calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    private fun updateDateTimeFields(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        if (year != -1) {
            selectedDate = String.format("%02d/%02d/%d", day, month + 1, year)
            editTextDate.setText(selectedDate)
        }
        if (hour != -1) {
            selectedTime = String.format("%02d:%02d", hour, minute)
            editTextTime.setText(selectedTime)
        }
    }

    private fun resetFormFields() {
        editTextTitle.setText("")
        editTextDescription.setText("")
        editTextAmount.setText("")
        editTextDate.setText("")
        editTextTime.setText("")
        selectedDate = ""
        selectedTime = ""
        transactionId = null

        // Resetea los Spinners y los deja en el primer item del array (default)
        spinnerType.setSelection(0)
        spinnerCategory.setSelection(0)
    }

    companion object {
        // Def. de keys para pasar los argumentos de edición
        const val TRANSACTION_ID_KEY          = "trans_id"
        const val TRANSACTION_TITLE_KEY       = "trans_title"
        const val TRANSACTION_DESCRIPTION_KEY = "trans_description"
        const val TRANSACTION_TYPE_KEY        = "trans_type"
        const val TRANSACTION_CATEGORY_KEY    = "trans_category"
        const val TRANSACTION_AMOUNT_KEY      = "trans_amount"
        const val TRANSACTION_DATE_KEY        = "trans_date"
        const val TRANSACTION_TIME_KEY        = "trans_time"

        @JvmStatic
        fun newInstance(transaction: Transaction? = null): RegistroTransaccionFragment {
            val fragment = RegistroTransaccionFragment()
            if (transaction != null) {
                val args = Bundle().apply {
                    putString(TRANSACTION_ID_KEY, transaction.id)
                    putString(TRANSACTION_TITLE_KEY, transaction.title)
                    putString(TRANSACTION_DESCRIPTION_KEY, transaction.description)
                    putString(TRANSACTION_TYPE_KEY, transaction.type)
                    putString(TRANSACTION_CATEGORY_KEY, transaction.category)
                    putDouble(TRANSACTION_AMOUNT_KEY, transaction.amount)
                    putString(TRANSACTION_DATE_KEY, transaction.date)
                    putString(TRANSACTION_TIME_KEY, transaction.time)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }
}