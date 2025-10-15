package com.mod5.ae3_abpro1_mislucas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

// Usamos View Binding (asumiendo que se configura en el build.gradle) para acceder a las vistas
class MainActivity : AppCompatActivity() {

    //private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usamos el método tradicional para inflar el layout, ya que View Binding no está disponible
        setContentView(R.layout.activity_main)

        // Carga el Fragmento inicial si es la primera vez que se crea la Activity
        if (savedInstanceState == null) {
            // Pasamos addToBackStack = false para que al presionar atrás no salga de la app en la vista principal
            loadFragment(ListaTransaccionesFragment.newInstance(), addToBackStack = false)
        }

        // Obtener la referencia a la barra de navegación inferior usando findViewById
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Configura el listener para la BottomNavigationView
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    loadFragment(ListaTransaccionesFragment.newInstance())
                    true
                }
                R.id.nav_register -> {
                    loadFragment(RegistroTransaccionFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Método central para gestionar la navegación entre Fragmentos.
     * Es llamado por los Fragmentos cuando se necesita cambiar de vista o editar.
     *
     * @param fragment El nuevo Fragmento a mostrar.
     * @param addToBackStack Si se añade la transacción a la pila "Atrás". Por defecto es true
     * para permitir volver al Fragmento anterior.
     */
    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            // Usamos el ID del FragmentContainerView definido en activity_main.xml
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null) // Permite volver con el botón "Atrás"
        }

        transaction.commit()
    }
}