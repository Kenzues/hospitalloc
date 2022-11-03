package com.jettech.hosprescue.Adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jettech.hosprescue.MainActivity
import com.jettech.hosprescue.Models.Requests
import com.jettech.hosprescue.R
import es.dmoral.toasty.Toasty
import java.lang.String
import java.util.*
import kotlin.collections.ArrayList

class RequestAdapter(private val context: Context) :
    RecyclerView.Adapter<RequestAdapter.ViewHolder>() {
    private lateinit var dialog: AlertDialog
    var activity: RequestAdapter? = null
    private lateinit var auth: FirebaseAuth
    private var requestslist = ArrayList<Requests>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        auth = Firebase.auth
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.list_requests,
                parent,
                false
            )
        )


    }

    override fun getItemCount(): Int {
        return if (requestslist.size == 0) {
            0
        } else {
            requestslist.size
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requests: Requests = requestslist[position]
        holder.bind(requests)

    }

    fun setRequest(requestsaddedlist: ArrayList<Requests>) {
        requestslist = requestsaddedlist
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patient_name = view.findViewById<TextView>(R.id.patient_name)
        val patient_phone = view.findViewById<TextView>(R.id.patient_phone)
        val patient_message = view.findViewById<TextView>(R.id.patient_message)
        val request_id = view.findViewById<TextView>(R.id.request_id)
        val latitude_cord = view.findViewById<TextView>(R.id.latitude_cord)
        val longitude_cord = view.findViewById<TextView>(R.id.longitude_cord)

        val user_token = view.findViewById<TextView>(R.id.user_token)

        val rescue_patient = view.findViewById<LinearLayout>(R.id.rescuepatient)

        

        private val patientname = patient_name!!
        private val patientphone = patient_phone!!
        private val patientmessage = patient_message!!
        private val requestid = request_id!!
        private val latitudecord = latitude_cord!!
        private val longitudecord = longitude_cord!!
        private val usertoken = user_token!!


        val btDelete = view.findViewById<LinearLayout>(R.id.deleteBin)
        val calling = view.findViewById<LinearLayout>(R.id.call)
        val tokenView = view.findViewById<TextView>(R.id.user_token)


        init {

            rescue_patient.setOnClickListener {

                val user = Firebase.auth.currentUser

                val db = FirebaseFirestore.getInstance()
                val stores = db.collection("Locations").document(user!!.uid)
                stores.get()
                    .addOnSuccessListener { document ->
                        if (document.data != null) {


                            val geoPoint = document.getGeoPoint("l")

                            val uri = String.format(
                                Locale.ENGLISH,
                                "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                                geoPoint!!.latitude,
                                geoPoint!!.longitude,
                                "Garage",
                                latitudecord.text.toString().toDouble(),
                                longitudecord.text.toString().toDouble(),
                                "patient"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)


                        }
                    }


            }

            btDelete.setOnClickListener {

                val dialog = Dialog(context)
                dialog.setContentView(R.layout.load_view)
                dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false)
                dialog.show()

                val user = Firebase.auth.currentUser
                val db = Firebase.firestore

                db.collection("users").document(user!!.uid).collection("patientRe")
                    .get().addOnSuccessListener { documentSnapshots ->
                        val document = documentSnapshots.documents.toString()
                        if (document.contains("[]")) {
                            Toasty.success(
                                context,
                                "No Request Available plz refresh.",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        } else {

                            db.collection("users").document(user!!.uid)
                                .collection("patientRe").document(requestid.text.toString())
                                .delete()
                                .addOnSuccessListener {
                                    Toasty.success(
                                        context,
                                        "Request has been deleted.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    requestslist.removeAt(position)
                                    notifyItemRemoved(position)
                                    //this line below gives you the animation and also updates the
                                    //list items after the deleted item
                                    notifyItemRangeChanged(position, getItemCount())

                                    (context as MainActivity).loadRequests()

                                    dialog.dismiss()
                                }


                        }
                    }


            }
            calling.setOnClickListener {

                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:${patient_phone.text}")
                context.startActivity(dialIntent)

            }
        }

        fun bind(requests: Requests) {
            patientname.text = requests.patient_name
            patientphone.text = requests.patient_phone
            patientmessage.text = requests.patient_message
            requestid.text = requests.request_id
            latitudecord.text = requests.latitude_cord
            longitudecord.text = requests.longitude_cord
            usertoken.text = requests.user_token


        }


    }

}








