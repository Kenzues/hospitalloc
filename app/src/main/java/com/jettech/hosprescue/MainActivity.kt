package com.jettech.hosprescue

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

import com.jettech.hosprescue.Adapters.RequestAdapter
import com.jettech.hosprescue.Models.Requests
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.imperiumlabs.geofirestore.GeoFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: Dialog

    private var viewAdapter: RequestAdapter? = null
    private var requestsaddedlist: ArrayList<Requests> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var mLinearLayout: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        auth = Firebase.auth
        val user = Firebase.auth.currentUser

        val Lat = intent.getStringExtra("lat")
        val Long = intent.getStringExtra("long")

        val dispL = findViewById<TextView>(R.id.latCord)
        val dispLong = findViewById<TextView>(R.id.longCord)
        val mySwitch = findViewById<Switch>(R.id.ghostMode)
        val logOut = findViewById<Button>(R.id.logout)
        dispL.text = Lat
        dispLong.text = Long

        val getLocation = findViewById<LinearLayout>(R.id.getLocation)
        val uploadLoc = findViewById<Button>(R.id.upload)

        getLocation.setOnClickListener {
            startActivity(Intent(this, HospMapActivity::class.java))
            finish()
        }

        mySwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                opened()
            } else {
                closed()
            }
        })

        logOut.setOnClickListener {
            dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.load_view)
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false)
            dialog.show()

            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            true

            dialog.dismiss()

        }


        uploadLoc.setOnClickListener {

            if (dispL.text!!.isEmpty()) {
                Toasty.error(this, "Set location.", Toast.LENGTH_LONG, true)
                    .show()

                return@setOnClickListener
            } else {

                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.load_view)
                dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false)
                dialog.show()

                val latlong = "${Lat},${Long}".split(",".toRegex()).toTypedArray()
                val latitude = latlong[0].toDouble()
                val longitude = latlong[1].toDouble()


                val collectionRef = FirebaseFirestore.getInstance().collection("Locations")
                val geoFirestore = GeoFirestore(collectionRef)

                geoFirestore.setLocation(user!!.uid, GeoPoint(latitude, longitude))

                val db = Firebase.firestore

                db.collection("Locations").document(user!!.uid).collection("hospitalInfo")
                    .get().addOnSuccessListener { documentSnapshots ->
                        val document = documentSnapshots.documents.toString()

                        if (document.contains("[]")) {
                            val docRef = db.collection("users").document(user!!.uid)
                            docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.data != null) {
                                        val userdata = document.data!!
                                        Log.d(
                                            "LoginActivity",
                                            "DocumentSnapshot data: ${document.data}"
                                        )
                                        val hospName = userdata.get("username").toString()
                                        val hospitalTitle = userdata.get("hospitalName").toString()
                                        val mobile = userdata.get("phone").toString()
                                        val closeOpen = userdata.get("closeOpen").toString()

                                        val data = hashMapOf(
                                            "hospitalName" to hospName.toString(),
                                            "mobile" to mobile.toString(),
                                            "hospitalTitle" to hospitalTitle.toString(),
                                            //"closeOpen" to closeOpen.toString(),

                                        )
                                        db.collection("Locations").document(user!!.uid)
                                            .collection("hospitalInfo")
                                            .add(data)
                                            .addOnCompleteListener {
                                                val hospId = it.result.id
                                                val newDocdata = hashMapOf("id" to hospId)
                                                db.collection("Locations").document(user!!.uid)
                                                    .collection("hospitalInfo").document(hospId)
                                                    .set(newDocdata, SetOptions.merge())

                                                dialog.dismiss()
                                                Toasty.success(
                                                    this,
                                                    "hospital location uploaded.",
                                                    Toast.LENGTH_LONG,
                                                    true
                                                ).show()
                                            }

                                    }


                                }

                        } else {
                            dialog.dismiss()

                        }

                    }
            }
        }
        
    }

    fun loadRequests() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore
        db.collection("users").document(user!!.uid).collection("patientRe")
            .get()
            .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot?> {
                override fun onComplete(@NonNull task: Task<QuerySnapshot?>) {
                    if (task.isSuccessful()) {

                        if (!task.getResult()!!.isEmpty()) {
                            requestsaddedlist.clear();

                            for (documentSnapshot in task.result!!) {
                                val availableRequest = documentSnapshot.data

                                if (!availableRequest!!.isEmpty()) {

                                    val requests = Requests()
                                    requests.latitude_cord =
                                        availableRequest.get("lat").toString()
                                    requests.longitude_cord =
                                        availableRequest.get("long").toString()
                                    requests.patient_message =
                                        availableRequest.get("carDescription").toString()
                                    requests.patient_name =
                                        availableRequest.get("patientName").toString()
                                    requests.patient_phone =
                                        availableRequest.get("patientPhone").toString()
                                    requests.request_id =
                                        availableRequest.get("id").toString()

                                    requests.user_token =
                                        availableRequest.get("userToken").toString()
                                    requestsaddedlist.add(requests)
                                    
                                } else {
                                    Toasty.error(this@MainActivity, "No Requests Available.", Toast.LENGTH_SHORT).show()

                                }

                            }
                            displayRequests(requestsaddedlist)

                        }

                    }
                }
            })

    }

    private fun displayRequests(requestsaddedlist: ArrayList<Requests>) {
        mLinearLayout = LinearLayoutManager(this)
        mLinearLayout!!.setReverseLayout(true);
        mLinearLayout!!.setStackFromEnd(true);
        viewAdapter = RequestAdapter(this)
        viewAdapter!!.setRequest(requestsaddedlist)
        recyclerView = findViewById<RecyclerView>(R.id.requestRecycler).apply {
            setHasFixedSize(true)
            layoutManager = mLinearLayout
            adapter =  viewAdapter


        }
    }


    private fun opened() {
        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()

        val db = Firebase.firestore

        val adminOddsData = hashMapOf(
            "closeOpen" to "1",
        )
        db.collection("users").document(user!!.uid)
            .update(adminOddsData as Map<String, Any>)
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toasty.success(
                        this@MainActivity,
                        "Hospital Opened",
                        Toasty.LENGTH_LONG
                    ).show()
                    dialog.dismiss()

                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    dialog.dismiss()
                }
            })
        
    }

    private fun closed() {

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()

        val db = Firebase.firestore
        val adminOddsData = hashMapOf(
            "closeOpen" to "0",
        )
        db.collection("users").document(user!!.uid)
            .update(adminOddsData as Map<String, Any>)
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toasty.success(
                        this@MainActivity,
                        "Hospital Closed",
                        Toasty.LENGTH_LONG
                    ).show()
                    dialog.dismiss()
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    dialog.dismiss()
                }
            })


    }

    private fun ghostSwitch() {
        auth = Firebase.auth
        val user = Firebase.auth.currentUser

        val db = Firebase.firestore

        val hospitalRef = db.collection("users").document(user!!.uid)
        hospitalRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    val userdata = document.data!!
                    val closeOpen = userdata.get("closeOpen").toString()
                    Log.d("checkGhost", closeOpen)
                    if (closeOpen.equals("0")) {
                        val mySwitch = findViewById<Switch>(R.id.ghostMode)
                        mySwitch.isChecked = false
                        // dialog.dismiss()
                    } else {
                        val mySwitch = findViewById<Switch>(R.id.ghostMode)
                        mySwitch.isChecked = true
                        
                    }
                    
                } else {
                    //  dialog.dismiss()
                }

            }


    }


    private fun userDetails() {
        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val hTitle = findViewById<TextView>(R.id.hospitalTitle)
        db.collection("users").document(user!!.uid).addSnapshotListener(
            EventListener { document, error ->
                if (error != null) {
                    Log.e("MainActivity", "Listen failed!", error)
                    return@EventListener
                }
                if (document != null) {
                    val accountNumber = document.data!!["hospitalName"].toString()
                    hTitle.text = accountNumber
                } else {
                    Log.d("MainActivity", "No such document")
                }
            })
    }
    
    override fun onResume() {
        super.onResume()
        ghostSwitch()
        userDetails()
        loadRequests()
    }

}