package com.jettech.hosprescue

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        if (user != null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
        }
        val reg = findViewById<Button>(R.id.regAcc)
        val loginButton = findViewById<Button>(R.id.loginBtn)
        reg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        val forgetPass = findViewById<Button>(R.id.forgetPass)
        forgetPass.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }
        loginButton.setOnClickListener {
            val loginEmail = findViewById<EditText>(R.id.userEmail)
            val loginPassword = findViewById<EditText>(R.id.password)
            if (loginEmail.text!!.isEmpty()) {
                Toasty.error(this, "Email field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()
                return@setOnClickListener
            }
            if (loginPassword.text!!.isEmpty()) {
                Toasty.error(this, "Password field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.load_view)
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false)
            dialog.show()
            auth.signInWithEmailAndPassword(loginEmail.text!!.trim().toString(), loginPassword.text!!.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("hapo",task.toString())
                        startActivity(Intent(this, PermissionsActivity::class.java))
                        finish()
                        dialog.dismiss()
                    } else {
                        Log.e("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toasty.error(baseContext, "Incorrect password or email: ",
                            Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                }
        }
    }

    fun solution(): Int {
        val A : IntArray = intArrayOf(10, 20, 30, 40, 50)
        var min = 1

        val b = A!!.sortedArray()
        for (i in 0 until b.size) {
            if (b[i] == min) {
                min++
            }
        }
        return min

    }
}