package com.jettech.hosprescue

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        val signUp = findViewById<TextView>(R.id.signUp)
        val forgotPass = findViewById<TextView>(R.id.forgotPass)
        val email = findViewById<EditText>(R.id.email)
        signUp.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
        forgotPass.setOnClickListener{
            if (email.text!!.isEmpty()) {
                Toasty.error(this, "Email field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }
            dialog = SpotsDialog.Builder().setContext(this).build()
            dialog.show()

            Firebase.auth.sendPasswordResetEmail(email.text!!.trim().toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()
                        Log.d("ForgotPasswordActivity", "Email sent.")
                        Toasty.success(this,"Please check your email address for reset instructions",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}