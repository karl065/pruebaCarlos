package carlos.castellanos.pruebacarlos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import carlos.castellanos.pruebacarlos.db.DBHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class Registro : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var mAuth: FirebaseAuth

    private var btnRegistro: FloatingActionButton? = null
    private var insEmailReg: EditText? = null
    private var insNombreReg: EditText? = null
    private var insContraReg: EditText? = null
    private var insConfContra: EditText? = null
    private var imgReg: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        mAuth = FirebaseAuth.getInstance()
        btnRegistro = findViewById(R.id.btnRegistroReg)
        insEmailReg = findViewById(R.id.insEmailReg)
        insNombreReg = findViewById(R.id.insNombreReg)
        insContraReg = findViewById(R.id.insContrasenaReg)
        insConfContra = findViewById(R.id.insConfirmarContra)
        imgReg = findViewById(R.id.imgReg)
        dbHelper = DBHelper(this)
        btnRegistro?.setOnClickListener {
            val nombre = insNombreReg?.text.toString().trim { it <= ' ' }
            val email = insEmailReg?.text.toString().trim { it <= ' ' }
            val contra = insContraReg?.text.toString().trim { it <= ' ' }
            val configContra = insConfContra?.text.toString().trim { it <= ' ' }
            if (nombre.isNotBlank() && email.isNotBlank() && contra.isNotBlank()) {
                if (contra.compareTo(configContra) == 0) {
                    mAuth.createUserWithEmailAndPassword(email, contra)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val idUser = mAuth.currentUser?.uid.toString()
                                dbHelper.insertarUsuarios(idUser, nombre, email)
                                mAuth.signInWithEmailAndPassword(email, contra)
                                Toast.makeText(this, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                applicationContext,
                                "Error en el registro",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.e("CreacionErradaUser", e.toString())
                        }
                } else {
                    Toast.makeText(this, "La contraseña no Coincide", Toast.LENGTH_LONG).show()
                }
            }else{
                    Toast.makeText(this, "Los campos no deben estar vacios", Toast.LENGTH_LONG).show()
            }
        }
    }
}