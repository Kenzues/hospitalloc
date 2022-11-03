package com.jettech.hosprescue

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class PermissionsActivity : AppCompatActivity() {




    private var user: FirebaseUser? = null

    private lateinit var auth: FirebaseAuth


    //    private var transList = ArrayList<Transactions>()
    private var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        if (ContextCompat.checkSelfPermission(this@PermissionsActivity, Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {

            val dialog = Dialog(this@PermissionsActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.load_view)
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false)
            dialog.show()

            user = Firebase.auth.currentUser
            val db = Firebase.firestore
            auth = Firebase.auth


            val docRef = db.collection("users").document(user!!.uid)

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {
                        val userdata = document.data!!
                        Log.d("LoginActivity", "DocumentSnapshot data: ${document.data}")
                        val userType = userdata.get("userType").toString()
                        Log.d("Loginuser", userType.toString())

                        if (userType.equals("1"))
                        {

                            dialog.dismiss()
                            startActivity(Intent(this@PermissionsActivity, MapActivity::class.java))
                            finish()
                        }else if (userType.equals("2"))
                        {

                            dialog.dismiss()
                            startActivity(Intent(this@PermissionsActivity, HospMapActivity::class.java))
                            finish()
                        }

                    }
                }

            return
        }

        val btnGrant = findViewById(R.id.btn_grant) as Button

        btnGrant.setOnClickListener {
            Dexter.withActivity(this@PermissionsActivity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {

                        val dialog = Dialog(this@PermissionsActivity)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.load_view)
                        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                        dialog.setCancelable(false)
                        dialog.show()

                        user = Firebase.auth.currentUser
                        val db = Firebase.firestore
                        auth = Firebase.auth


                        val docRef = db.collection("users").document(user!!.uid)

                        docRef.get()
                            .addOnSuccessListener { document ->
                                if (document.data != null) {
                                    val userdata = document.data!!
                                    Log.d("LoginActivity", "DocumentSnapshot data: ${document.data}")
                                    val userType = userdata.get("userType").toString()

                                    if (userType.equals("1"))
                                    {

                                        dialog.dismiss()
                                        startActivity(Intent(this@PermissionsActivity, MapActivity::class.java))
                                        finish()
                                    }else if (userType.equals("2"))
                                    {

                                        dialog.dismiss()
                                        startActivity(Intent(this@PermissionsActivity,  HospMapActivity::class.java))
                                        finish()
                                    }

                                }
                            }

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(this@PermissionsActivity)
                            builder.setTitle("Permission Denied")
                                .setMessage("Permission to access device location is permanently denied. you need to go to setting to allow the permission.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    intent.data = Uri.fromParts("package", packageName, null)
                                })
                                .show()
                        } else {
                            Toast.makeText(this@PermissionsActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                })
                .check()
        }
    }
}