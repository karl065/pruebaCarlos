package carlos.castellanos.pruebacarlos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import carlos.castellanos.pruebacarlos.db.DBHelper
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var mAuth: FirebaseAuth

    private var btnIngresar: FloatingActionButton? = null
    private var btnRegistrar: FloatingActionButton? = null
    private var insEmail: EditText? = null
    private var insNombre: EditText? = null
    private var insContra: EditText? = null
    private var imgLogin: ImageButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistro)
        insEmail = findViewById(R.id.insEmail)
        insNombre = findViewById(R.id.insNombre)
        insContra = findViewById(R.id.insContrasena)
        imgLogin = findViewById(R.id.imgLogin)

        mAuth = FirebaseAuth.getInstance()

        dbHelper = DBHelper(this)

        btnIngresar?.setOnClickListener {
            val nombre = insNombre?.text.toString().trim { it <= ' ' }
            val email = insEmail?.text.toString().trim { it <= ' ' }
            val contra = insContra?.text.toString().trim { it <= ' ' }
            mAuth.signInWithEmailAndPassword(email, contra)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        val idUser = mAuth.currentUser?.uid.toString()
                        dbHelper.insertarUsuarios(idUser, nombre, email)
                        val intent =
                            Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show()
                    Log.e("ErrorLogin", e.toString())
                }
        }
        btnRegistrar?.setOnClickListener {
            val intent = Intent(applicationContext, Registro::class.java)
            startActivity(intent)
        }
    }
}