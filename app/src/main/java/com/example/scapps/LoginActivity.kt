package com.example.scapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //dekalrasi Variable untuk Firebase dkk
    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //get Instance
        mAuth = FirebaseAuth.getInstance()

        //onClick untuk Daftar
        tvdaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Event on Click
    fun btnLoginPage(view: View) {
        loginToFireBase(
            etEmail.text.toString(),
            etEmail.text.toString()
        )
    }

    // LoadPost Berdasarkan email dan uid(unik ID)
    fun LoadPost() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)
            startActivity(intent)
        }
    }

    //Login FireBase
    fun loginToFireBase(email: String, password: String) {
        //firebase login by email & password
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                //Jika Sukses
                if (task.isSuccessful) {
                    var currentUser = mAuth!!.currentUser
                    Toast.makeText(
                        applicationContext, "Login Sukses", Toast.LENGTH_LONG
                    ).show()

                    //Save data ke berdasarkan input pada edit Text
                    myRef.child("Users").child(currentUser!!.uid).child("email")
                        .setValue(currentUser.email)
                    LoadPost()

                    //Jika Gagal
                } else {
                    Toast.makeText(applicationContext, "Login Gagal", Toast.LENGTH_LONG).show()
                }
            }
    }
}
