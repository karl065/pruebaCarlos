package carlos.castellanos.pruebacarlos.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper (context: Context) : SQLiteOpenHelper(
    context, "Decimetrix.db", null, 1) {

    private val db: SQLiteDatabase = this.writableDatabase
    private  val values: ContentValues = ContentValues()

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE USUARIOS(" +
                "_id VARCHAR," +
                "NOMBRE VARCHAR," +
                "EMAIL VARCHAR" +
                ")")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS USUARIOS")
        onCreate(db)
    }

    fun insertarUsuarios(id: String, nombre: String, email: String) {
        values.put("_id", id)
        values.put("NOMBRE",nombre)
        values.put("EMAIL",email)
        db.insert("USUARIOS",null, values)
        db.close()
    }

    fun consultarUsuarios(): Cursor? {
        return db.rawQuery("SELECT * FROM USUARIOS", null)
    }

    fun eliminarUsuario(id: String) {
        db.execSQL("DELETE FROM USUARIOS WHERE _id = '$id'")
        db.close()
    }

}