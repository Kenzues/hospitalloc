package com.jettech.hosprescue

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import es.dmoral.toasty.Toasty

class RegisterActivity : AppCompatActivity() {
    private var countryCode: String? = null
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 1234
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        val login = findViewById<Button>(R.id.loginReg)
        val regpatient = findViewById<Button>(R.id.regpatientBtn)
        val hospReg = findViewById<Button>(R.id.reghospBtn)
        val patientLayout = findViewById<View>(R.id.patientLayout) as LinearLayout
        val hospitalLayout = findViewById<View>(R.id.hospitalLayout) as LinearLayout
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val fullname = findViewById<EditText>(R.id.patientuserName)
        val email = findViewById<EditText>(R.id.patientemail)
        val password = findViewById<EditText>(R.id.patientPassword)
        val cnfPasword = findViewById<EditText>(R.id.patientCnfPasword)
        val mobile = findViewById<EditText>(R.id.patientmobile)
        val hospitalName = findViewById<EditText>(R.id.hospitalName)
        val hospName = findViewById<EditText>(R.id.hospName)
        val hospEmail = findViewById<EditText>(R.id.hospEmail)
        val hospPaswords = findViewById<EditText>(R.id.hospPaswords)
        val hospCnfPassword = findViewById<EditText>(R.id.hospCnfPassword)
        val hospMobile = findViewById<EditText>(R.id.hospMobile)
        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            run {
                when (checkedId) {
                    R.id.radioPersonalAccount -> {
                        patientLayout.visibility = View.VISIBLE
                        hospitalLayout.visibility = View.GONE
                    }
                    R.id.radioGroupsAccount -> {

                        hospitalLayout.visibility = View.VISIBLE
                        patientLayout.visibility = View.GONE


                    }

                }
            }
        }

        regpatient.setOnClickListener {
            if (fullname.text!!.isEmpty()) {
                Toasty.error(this, "fullname field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }

            if (email.text!!.isEmpty()) {
                Toasty.error(this, "Email field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }

            if (mobile.text!!.isEmpty()) {
                Toasty.error(this, "Phone number field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }

            if (password.text!!.isEmpty()) {
                Toasty.error(this, "Password field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }
            if (cnfPasword.text!!.isEmpty()) {
                Toasty.error(
                    this,
                    "Confirm password field cannot be blank.",
                    Toast.LENGTH_LONG,
                    true
                ).show()

                return@setOnClickListener
            }

            if (password.text!!.trim().length < 6) {
                Toasty.error(
                    this,
                    "Password length must be 6 characters and above",
                    Toast.LENGTH_LONG,
                    true
                )
                    .show()
                return@setOnClickListener
            }
            if (password.text!!.toString() != cnfPasword.text!!.toString()) {
                Toasty.error(this, "Passwords don't match", Toast.LENGTH_LONG, true)
                    .show()
                return@setOnClickListener
            }



            val providers = arrayListOf(
                AuthUI.IdpConfig.PhoneBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls(
                        "https://gloib.com/terms.html",
                        "https://gloib.com/privacy.html"
                    )
                    .build(),


                RC_SIGN_IN
            )




        }

        hospReg.setOnClickListener {
            if (hospitalName.text!!.isEmpty()) {
                Toasty.error(
                    this,
                    "hospital name field cannot be blank.",
                    Toast.LENGTH_LONG,
                    true
                )
                    .show()

                return@setOnClickListener
            }
            if (hospName.text!!.isEmpty()) {
                Toasty.error(this, "username field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }
            if (hospEmail.text!!.isEmpty()) {
                Toasty.error(this, "Email field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }

            if (hospMobile.text!!.isEmpty()) {
                Toasty.error(this, "Phone number field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }



            if (hospPaswords.text!!.isEmpty()) {
                Toasty.error(this, "Password field cannot be blank.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            }
            if (hospCnfPassword.text!!.isEmpty()) {
                Toasty.error(
                    this,
                    "Confirm password field cannot be blank.",
                    Toast.LENGTH_LONG,
                    true
                ).show()

                return@setOnClickListener
            }



            if (hospPaswords.text!!.trim().length < 6) {
                Toasty.error(
                    this,
                    "Password length must be 6 characters and above",
                    Toast.LENGTH_LONG,
                    true
                )
                    .show()
                return@setOnClickListener
            }
            if (hospPaswords.text!!.toString() != hospCnfPassword.text!!.toString()) {
                Toasty.error(this, "Passwords don't match", Toast.LENGTH_LONG, true)
                    .show()
                return@setOnClickListener
            }



            val providers = arrayListOf(
                AuthUI.IdpConfig.PhoneBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls(
                        "https://gloib.com/terms.html",
                        "https://gloib.com/privacy.html"
                    )
                    .build(),
                RC_SIGN_IN
            )


        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The last parameter value of shouldHandleResult() is the value we pass to setRequestCode().
        // If we do not call setRequestCode(), we can ignore the last parameter.

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {



                if (radioGroup.getCheckedRadioButtonId() == R.id.radioPersonalAccount)
                {
                    patientAcc ()
                }
                else if (radioGroup.getCheckedRadioButtonId() == R.id.radioGroupsAccount)
                {
                    hospitalAcc()
                }


            }else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_LONG).show()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

            }
            else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, response!!.error!!.errorCode, Toast.LENGTH_LONG).show()
            }


            super.onActivityResult(
                requestCode,
                resultCode,
                data
            )   // This line is REQUIRED in fragment mode


        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show()
        }

    }

    private fun patientAcc () {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()


        val email = findViewById<TextView>(R.id.patientemail)
        val password = findViewById<TextView>(R.id.patientPassword)
        // Successfully signed in

        val currentUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(
            email.text.toString(),
            password.text.toString()
        )
        currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "linkWithCredential:success")
                    val user = task.result?.user
                    Log.e("RegisterActivity", user!!.phoneNumber!!)

                    val dialog = Dialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.load_view)
                    dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(false)

                    val fullname = findViewById<TextView>(R.id.patientuserName)
                    val email = findViewById<TextView>(R.id.patientemail)


                    val db = Firebase.firestore

                    val userData = hashMapOf(

                        "fullname" to fullname.text.toString(),
                        "email" to email.text.toString(),
                        "userType" to "1",
                        "phone" to user.phoneNumber,
                        "id" to user.uid,

                        )

                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.e(
                                "RegisterActivity",
                                "DocumentSnapshot successfully written!"
                            )
                            val profileUpdates = userProfileChangeRequest {
                                displayName =
                                    fullname.text.toString()

                            }

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        Log.d(
                                            "RegisterActivity",
                                            "User profile updated."
                                        )
                                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                            OnCompleteListener { task ->
                                                if (!task.isSuccessful) {
                                                    Log.w(
                                                        "MainActivity",
                                                        "Fetching FCM registration token failed",
                                                        task.exception
                                                    )
                                                    return@OnCompleteListener
                                                }

                                                // Get new FCM registration token
                                                val token = task.result

                                                db.collection("users")
                                                    .document(user.uid)
                                                    .update("token", token)
                                                    .addOnSuccessListener {
                                                        startActivity(
                                                            Intent(
                                                                this,
                                                                PermissionsActivity::class.java
                                                            )
                                                        )
                                                        finish()
                                                        dialog.dismiss()

                                                    }
                                            })
                                    }
                                }


                        }
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Log.e("RegisterActivity", "Error writing document", e)
                        }


                } else {
                    Log.w("RegisterActivity", "linkWithCredential:failure", task.exception)

                    Toasty.error(
                        baseContext, "Authentication failed: " + task.exception,
                        Toast.LENGTH_LONG
                    ).show()

                }


            }

    }

    private fun hospitalAcc(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()

        val email = findViewById<TextView>(R.id.hospEmail)
        val password = findViewById<TextView>(R.id.hospPaswords)
        // Successfully signed in

        val currentUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(
            email.text.toString(),
            password.text.toString()
        )
        currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "linkWithCredential:success")
                    val user = task.result?.user
                    Log.e("RegisterActivity", user!!.phoneNumber!!)

                    val dialog = Dialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.load_view)
                    dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(false)

                    val hospitalName = findViewById<TextView>(R.id.hospitalName)
                    val hospName = findViewById<TextView>(R.id.hospName)
                    val hospEmail = findViewById<TextView>(R.id.hospEmail)


                    val db = Firebase.firestore
                    val userData = hashMapOf(

                        "username" to hospName.text.toString(),
                        "hospitalName" to hospitalName.text.toString(),
                        "email" to hospEmail.text.toString(),
                        "userType" to "2",
                        "phone" to user.phoneNumber,
                        "id" to user.uid,
                        "closeOpen" to "0",

                        )

                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.e(
                                "RegisterActivity",
                                "DocumentSnapshot successfully written!"
                            )
                            val profileUpdates = userProfileChangeRequest {
                                displayName =
                                    hospName.text.toString()

                            }

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        Log.d(
                                            "RegisterActivity",
                                            "User profile updated."
                                        )
                                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                            OnCompleteListener { task ->
                                                if (!task.isSuccessful) {
                                                    Log.w(
                                                        "MainActivity",
                                                        "Fetching FCM registration token failed",
                                                        task.exception
                                                    )
                                                    return@OnCompleteListener
                                                }

                                                // Get new FCM registration token
                                                val token = task.result

                                                db.collection("users")
                                                    .document(user.uid)
                                                    .update("token", token)
                                                    .addOnSuccessListener {
                                                        startActivity(
                                                            Intent(
                                                                this,
                                                                PermissionsActivity::class.java
                                                            )
                                                        )
                                                        finish()
                                                        dialog.dismiss()

                                                    }
                                            })
                                    }
                                }


                        }
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Log.e("RegisterActivity", "Error writing document", e)
                        }


                } else {
                    Log.w("RegisterActivity", "linkWithCredential:failure", task.exception)

                    Toasty.error(
                        baseContext, "Authentication failed: " + task.exception,
                        Toast.LENGTH_LONG
                    ).show()

                }


            }

    }
}