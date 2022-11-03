package com.jettech.hosprescue

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*


import com.google.firebase.messaging.FirebaseMessaging
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.auth.zzl.getToken
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import es.dmoral.toasty.Toasty
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationProviderClients: FusedLocationProviderClient
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLastKnownLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private lateinit var dialog: Dialog
    private val mapView: View? = null
    private var gooleMapView: View? = null
    private val DEFAULT_ZOOM = 16f
    var locationPermissionGranted = false
    var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    // two array list for our lat long and location Name;
    private var latLnhosprayList: ArrayList<LatLng>? = null
    private var locationNameArraylist: ArrayList<String>? = null
    private var uidArraylist: ArrayList<String>? = null
    var hospitalNumber: String = ""
    var description: String = ""
    var hospitalUid: String = ""

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth


    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        

        latLnhosprayList = ArrayList()
        locationNameArraylist = ArrayList()
        uidArraylist = ArrayList()

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@MapActivity)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val logout = findViewById<Button>(R.id.logoutUser)
        
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapActivity)



        gooleMapView = mapFragment.getView();

        logout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            true
        }


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        loadHospitals()
        mMap!!.setOnMarkerClickListener { marker -> // on marker click we are getting the title of our marker
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.load_view)
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false)
            dialog.show()
            val markerName = marker.title
            hospitalUid = marker.snippet.toString()
            
            val viewGroup = findViewById<ViewGroup>(R.id.root)
            val dialogView: View =
                LayoutInflater.from(this).inflate(R.layout.navigate_view, viewGroup, false)
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                .setCancelable(false)
            builder.setView(dialogView)

            val alertDialog: AlertDialog = builder.create()
            val db = Firebase.firestore
            val docRef = db.collection("users").document(hospitalUid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {
                        val userdata = document.data!!
                        val hospitalName = userdata.get("hospitalName").toString()
                        val hospitalStatus = userdata.get("closeOpen").toString()
                        hospitalNumber = userdata.get("phone").toString()


                        val hospName = dialogView.findViewById<TextView>(R.id.titleDisp)
                        val number = dialogView.findViewById<TextView>(R.id.call)
                        val hospState = dialogView.findViewById<TextView>(R.id.status)

                        hospName.text = hospitalName + " " + "hospital"
                        number.text = hospitalNumber

                        if (hospitalStatus.contains("0")) {
                            hospState.text = "CLOSED"
                            hospState.setTextColor(
                                ContextCompat.getColor(
                                    this@MapActivity,
                                    R.color.red
                                )
                            )
                        } else {
                            hospState.text = "OPEN"
                            hospState.setTextColor(
                                ContextCompat.getColor(
                                    this@MapActivity,
                                    R.color.green
                                )
                            )
                        }


                    }
                }
            builder.apply {

                val number = dialogView.findViewById<TextView>(R.id.call)
                val callhospital = dialogView.findViewById<LinearLayout>(R.id.calling)
                val send = dialogView.findViewById<Button>(R.id.sendM)
                val drive = dialogView.findViewById<Button>(R.id.drive)
                val cancel = dialogView.findViewById<Button>(R.id.exit)


                val textMessage = dialogView.findViewById<EditText>(R.id.textMe)


                number.text = hospitalNumber

                callhospital.setOnClickListener {

                    val dialIntent = Intent(Intent.ACTION_DIAL)
                    dialIntent.data = Uri.parse("tel:${number.text}")
                    context.startActivity(dialIntent)

                }
                send.setOnClickListener {

                    if (textMessage.text!!.isEmpty()) {


                        Toasty.error(
                            this@MapActivity, "Description of car condition", Toasty.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    } else {

                        description = textMessage.text.toString()

                        patientInfo()

                        alertDialog.dismiss()
                    }


                }

                drive.setOnClickListener {

                    val db = FirebaseFirestore.getInstance()
                    val stores = db.collection("Locations").document(hospitalUid)
                    stores.get()
                        .addOnSuccessListener { document ->
                            if (document.data != null) {

                                val geoPoint = document.getGeoPoint("l")

                                val uri = java.lang.String.format(
                                    Locale.ENGLISH,
                                    "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                                    mLastKnownLocation!!.latitude,
                                    mLastKnownLocation!!.longitude,
                                    "my location",
                                    geoPoint!!.latitude,
                                    geoPoint!!.longitude,
                                    "${markerName}"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)


                            }
                        }

                    Toasty.success(
                        this@MapActivity, "calculating", Toasty.LENGTH_LONG
                    ).show()
                    alertDialog.dismiss()
                }

                cancel.setOnClickListener {


                    alertDialog.dismiss()

                }

                alertDialog.show()
            }

            dialog.dismiss()

            Toast.makeText(this@MapActivity, "$markerName", Toast.LENGTH_SHORT)
                .show()
            false

        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
//        getDeviceLocation()
        getLastLocation()


    }

    private fun patientInfo() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()

        auth = Firebase.auth
        val user = Firebase.auth.currentUser


        val db = Firebase.firestore
        val patientRef = db.collection("users").document(user!!.uid)
        patientRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    val userdata = document.data!!
                    val patientName = userdata.get("fullname").toString()
                    val patientPhone = userdata.get("phone").toString()
                    val patientToken = userdata.get("token").toString()

                    val carDescription = description

                    val patientLocation = LatLng(
                        mLastKnownLocation!!.getLatitude(),
                        mLastKnownLocation!!.getLongitude()
                    )

                    val requestData = hashMapOf(
                        "patientName" to patientName,
                        "patientPhone" to patientPhone,
                        "carDescription" to carDescription,
                        "userToken" to patientToken.toString(),
                        "lat" to patientLocation.latitude,
                        "long" to patientLocation.longitude,
                    )
                    db.collection("users").document(hospitalUid).collection("patientRe")
                        .add(requestData)
                        .addOnCompleteListener {
                            val requestId = it.result.id
                            val newDocdata = hashMapOf("id" to requestId)
                            db.collection("users").document(hospitalUid)
                                .collection("patientRe").document(requestId)
                                .set(newDocdata, SetOptions.merge())

                            Toasty.success(
                                this@MapActivity, "Request send,please wait", Toasty.LENGTH_LONG
                            ).show()
                            dialog.dismiss()
                        }

                }
            }

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        mLastKnownLocation = location
                        mMap!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), DEFAULT_ZOOM
                            )
                        )

                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

            mLastKnownLocation = mLastLocation
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mLastLocation.latitude,
                        mLastLocation.longitude
                    ), DEFAULT_ZOOM
                )
            )
        }
    }


    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {


        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        if (mapView != null && mapView.findViewById<View?>("1".toInt()) != null) {
            val locationButton =
                (mapView.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 180)
        }

        //check if gps is enabled or not and then request user to enable it
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this@MapActivity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener(this@MapActivity,
            OnSuccessListener<LocationSettingsResponse?> {
                // getDeviceLocation()
            })
        task.addOnFailureListener(this@MapActivity, OnFailureListener { e ->


            if (e is ResolvableApiException) {

                try {

                    e.startResolutionForResult(this@MapActivity, 51)

                } catch (e1: IntentSender.SendIntentException) {

                    e1.printStackTrace()
                }
            }
        }

        )
        //getDeviceLocation()

    }


    private fun loadHospitals() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.load_view)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.show()

        val db = FirebaseFirestore.getInstance()
        val stores = db.collection("Locations")
        stores.get().addOnCompleteListener { task ->

            if (task.result.documents.isEmpty()){
                Toast.makeText(this,"Sorry no hospital service providers in this region",Toast.LENGTH_LONG).show()
                dialog.dismiss()

            }else{
                if (task.isSuccessful) {
                    for (document in task.result) {


                        val geoPoint = document.getGeoPoint("l")
                        val hospitalId = document.id

                        val lat = geoPoint!!.latitude
                        val lng = geoPoint.longitude
                        val hospitalLocation = LatLng(lat, lng)


                        val db = FirebaseFirestore.getInstance()
                        val hospitalInfo = db.collection("Locations").document(hospitalId)
                            .collection("hospitalInfo")
                        hospitalInfo.get().addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                for (document in task.result) {

                                    val name = document.get("hospitalTitle").toString()
                                    Log.d("ishakuwa", name.toString())

                                    latLnhosprayList!!.add(hospitalLocation);
                                    locationNameArraylist!!.add(name);
                                    uidArraylist!!.add(hospitalId);



                                    for (i in latLnhosprayList!!.indices) {
                                        Log.d("ERICK", latLnhosprayList.toString())


                                        // adding marker to each location on google maps
                                        mMap!!.addMarker(
                                            MarkerOptions().position(latLnhosprayList!![i])
                                                .snippet(uidArraylist!![i]).icon(
                                                    BitmapDescriptorFactory.fromBitmap(
                                                        getMarkerBitmapFromView(R.drawable.hospital)!!
                                                    )
                                                )
                                                .title(locationNameArraylist!![i] + " " + "hospital")
                                        )

                                    }


                                }
                            }else{
                                dialog.dismiss()
                            }
                        }
                        dialog.dismiss()

                    }
                } else {
                    dialog.dismiss()
//                                     Log.w(TAG, "Error getting documents.", task.exception)
                }
            }

        }
    }


    private fun getMarkerBitmapFromView(@DrawableRes resId: Int): Bitmap? {

        val customMarkerView: View =
            (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.view_custom_marker,
                null
            )

        val markerImageView: ImageView =
            customMarkerView.findViewById(R.id.profile_image) as ImageView
        markerImageView.setImageResource(resId)
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        customMarkerView.layout(
            0,
            0,
            customMarkerView.getMeasuredWidth(),
            customMarkerView.getMeasuredHeight()
        )
        customMarkerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable: Drawable = customMarkerView.getBackground()
        if (drawable != null) drawable.draw(canvas)
        customMarkerView.draw(canvas)
        return returnedBitmap
    }


    private var exit: Boolean? = false
    override fun onBackPressed() {
        if (exit!!) {
            moveTaskToBack(true)// finish activity
        } else {
            Toast.makeText(
                this, "Press Back again to Exit.",
                Toast.LENGTH_SHORT
            ).show()
            exit = true
            Handler().postDelayed({ exit = false }, 3 * 1000)

        }

    }

    override fun onResume() {
        super.onResume()
        getLastLocation()
        loadHospitals()
    }

//    companion object {
//        const val TOPIC = "/topics/myTopic2"
//    }


}
