package com.example.mobile_app_sensores

import android.annotation.TargetApi
import android.app.*
import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentSender
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.SavedStateViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.widget.*
import androidx.core.content.ContextCompat
import java.lang.Exception

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    //private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: GeofenceViewModel
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val endereco = "http://192.168.1.9:5000"

    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    var pessoas_cantina = ""

    //variáveis a usar na ementa activity
    var ac1 = "Nenhum"
    var ac2 = "Nenhum"
    var acucar = "0 g"
    var energia = "0 Kcal"
    var fibras = "0 g"
    var hidratos = "0 g"
    var lipidos = "0 g"
    var saturados = "0 g"
    var prato = "Nenhum"
    var proteina = "0 g"
    var sal = "0 g"
    var sopa = "Nenhuma"

    //variáveis a usar na ementa vegan activity
    var ac1_vegan = "Nenhum"
    var ac2_vegan = "Nenhum"
    var acucar_vegan = "0 g"
    var energia_vegan = "0 Kcal"
    var fibras_vegan = "0 g"
    var hidratos_vegan = "0 g"
    var lipidos_vegan = "0 g"
    var saturados_vegan = "0 g"
    var prato_vegan = "Nenhum"
    var proteina_vegan = "0 g"
    var sal_vegan = "0 g"
    var sopa_vegan = "Nenhuma"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        setContentView(R.layout.activity_main)

        //Geofencing Stuff
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, SavedStateViewModelFactory(this.application,
            this)
        ).get(GeofenceViewModel::class.java)
        //binding.viewmodel = viewModel
        geofencingClient = LocationServices.getGeofencingClient(this)

        //criar canal para notificações
        createChannel(this)

        if (intent.getStringExtra("data") == null){//1º vez
            state_now()
            getPessoasCantinaNoMomento()
            getEmentaDiaAtual()
            getEmentaVeganDiaAtual()
        }
        else{
            state_before()
            getPessoasCantina()
            getEmenta()
            getEmentaVegan()
        }
        pickDate()
        verEmenta()
        verEmentaVegan()
    }

    /**
     * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
     * method should be called after the user has granted the location permission.  If there are
     * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
     * is now "active."
     */
    private fun addGeofenceForClue() {
        if (viewModel.geofenceIsActive()) {
            Log.e(TAG, "Já está ativa")
            return
        }
        val currentGeofenceIndex = viewModel.nextGeofenceIndex()
        if(currentGeofenceIndex >= GeofencingConstants.NUM_LANDMARKS) {
            removeGeofences()
            viewModel.geofenceActivated()
            return
        }
        val currentGeofenceData = GeofencingConstants.LANDMARK_DATA[currentGeofenceIndex]

        // Build the Geofence Object
        val geofence = Geofence.Builder()
            .setRequestId(currentGeofenceData.id)// Set the request ID, string to identify the geofence.
            .setCircularRegion(currentGeofenceData.latLong.latitude, // Set the circular region of this geofence.
                currentGeofenceData.latLong.longitude,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)// Set the expiration duration of the geofence. This geofence gets automatically removed after this period of time.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)// Set the transition types of interest. Alerts are only generated for these transition. We track entry and exit transitions in this sample.
            .build()


        var longitude = 0.0;
        var latitude = 0.0;

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        val location = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            longitude = 0.0;
            latitude = 0.0;
            return
        }else{
            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                longitude = location.getLongitude()
            };
            if (location != null) {
                latitude = location.getLatitude()
            }
            else{
                latitude = 0.0;
            }
        }

        Log.e(latitude.toString(), geofence.requestId)
        Log.e(longitude.toString(), geofence.requestId)

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)// Add the geofences to be monitored by geofencing service.
            .build()

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
                        Log.e("Geofence Added", geofence.requestId)
                        // Tell the viewmodel that we've reached the end of the game and
                        // activated the last "geofence" --- by removing the Geofence.
                        //viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        // Failed to add geofences.
                        Toast.makeText(this@MainActivity, "Geofences Not Added",
                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w(TAG, it.message.toString())
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, "Geofences Removed")
                Toast.makeText(applicationContext, "Geofences Removed", Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, "Geofences Not Removed")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    /**
    *  When we get the result from asking the user to turn on device location, we call
    *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
    *  we don't resolve the check to keep the user from seeing an endless loop.
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    /**
     *  When the user clicks on the notification, this method will be called, letting us know that
     *  the geofence has been triggered, and it's time to move to the next one in the treasure
     *  hunt.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    /**
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            return
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }


    /**
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                return@addOnFailureListener
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
            }
        }
    }

    /**
     *  Determines whether the app has the appropriate permissions across Android 10+ and all other
     *  Android versions.
     */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /**
     *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
     */
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissionsArray,
            resultCode
        )
    }

    //Funções Adicionais
    private fun state_now(){

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"))
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH) + 1//porque vem em indice
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        //para aparecer 0 à esquerda
        val day_string = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
        val month_string = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year_string = cal.get(Calendar.YEAR).toString()
        val hour_string = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
        val minute_string = String.format("%02d", cal.get(Calendar.MINUTE))

        tv_textTime.text = "$day_string-$month_string-$year_string $hour_string:$minute_string"
    }

    private fun state_before(){
        val data = intent.getStringExtra("data").toString()
        pessoas_cantina = intent.getStringExtra("pessoas_cantina").toString()
        pessoas_info.text = pessoas_cantina

        val elementos = data.split(" ")
        var dma = elementos[0].split("-")
        var hms = elementos[1].split(":")

        savedDay = Integer.parseInt(dma[0])
        savedMonth = Integer.parseInt(dma[1])
        savedYear = Integer.parseInt(dma[2])

        savedHour = Integer.parseInt(hms[0])
        savedMinute = Integer.parseInt(hms[1])

        tv_textTime.text = dma[0] + "-" + dma[1] + "-" + dma[2] + " " + hms[0] + ":" + hms[1]
    }

    private fun getDateTimeCalendar(){
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH) + 1
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate(){

        btn_timePicker.setOnClickListener {
            getDateTimeCalendar()

            DatePickerDialog(this, this, year, month-1, day).show()
            pessoas_info.text = pessoas_cantina
        }
    }


    private fun getEmentaVeganDiaAtual(){
        val data = "$year-$month-$day $hour:$minute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")
        val txt = endereco + "/ementa_vegan.json"
        val url = URL(txt+parameter)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject

                    if (jsonObject.getString("Acompanhamento_1_Vegan") != "") {
                        ac1_vegan = jsonObject.getString("Acompanhamento_1_Vegan")
                    }

                    if (jsonObject.getString("Acompanhamento_2_Vegan") != "") {
                        ac2_vegan = jsonObject.getString("Acompanhamento_2_Vegan")
                    }

                    if (jsonObject.getString("Açúcares_Vegan") != "") {
                        acucar_vegan = jsonObject.getString("Açúcares_Vegan")
                    }

                    if (jsonObject.getString("Energia_Vegan") != "") {
                        energia_vegan = jsonObject.getString("Energia_Vegan") + " Kcal"
                    }

                    if (jsonObject.getString("Fibras_Vegan") != "") {
                        fibras_vegan = jsonObject.getString("Fibras_Vegan") + " g"
                    }

                    if (jsonObject.getString("Hidratos_carbono_Vegan") != "") {
                        hidratos_vegan = jsonObject.getString("Hidratos_carbono_Vegan") + " g"
                    }

                    if (jsonObject.getString("Lípidos_Vegan") != "") {
                        lipidos_vegan = jsonObject.getString("Lípidos_Vegan") + " g"
                    }

                    if (jsonObject.getString("Lípidos_saturados_Vegan") != "") {
                        saturados_vegan = jsonObject.getString("Lípidos_saturados_Vegan") + " g"
                    }

                    if (jsonObject.getString("Prato_Vegan") != "") {
                        prato_vegan = jsonObject.getString("Prato_Vegan")
                    }

                    if (jsonObject.getString("Proteína_Vegan") != "") {
                        proteina_vegan = jsonObject.getString("Proteína_Vegan") + " g"
                    }

                    if (jsonObject.getString("Sal_Vegan") != "") {
                        sal_vegan = jsonObject.getString("Sal_Vegan") + " g"
                    }

                    if (jsonObject.getString("Sopa_Vegan") != "") {
                        sopa_vegan = jsonObject.getString("Sopa_Vegan")
                    }
                }

            }
        } catch (e : Exception){
            btn_ementa_vegan.isEnabled = false
            btn_ementa_vegan.isClickable = false
            btn_ementa_vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            println("Host Unreachable")
        }
    }


    private fun getEmentaDiaAtual(){
        val data = "$year-$month-$day $hour:$minute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")
        val txt = endereco + "/ementa.json"
        val url = URL(txt+parameter)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject

                    if (jsonObject.getString("Acompanhamento_1") != "") {
                        ac1 = jsonObject.getString("Acompanhamento_1")
                    }

                    if (jsonObject.getString("Acompanhamento_2") != "") {
                        ac2 = jsonObject.getString("Acompanhamento_2")
                    }

                    if (jsonObject.getString("Açúcares") != "") {
                        acucar = jsonObject.getString("Açúcares")
                    }

                    if (jsonObject.getString("Energia") != "") {
                        energia = jsonObject.getString("Energia") + " Kcal"
                    }

                    if (jsonObject.getString("Fibras") != "") {
                        fibras = jsonObject.getString("Fibras") + " g"
                    }

                    if (jsonObject.getString("Hidratos_carbono") != "") {
                        hidratos = jsonObject.getString("Hidratos_carbono") + " g"
                    }

                    if (jsonObject.getString("Lípidos") != "") {
                        lipidos = jsonObject.getString("Lípidos") + " g"
                    }

                    if (jsonObject.getString("Lípidos_saturados") != "") {
                        saturados = jsonObject.getString("Lípidos_saturados") + " g"
                    }

                    if (jsonObject.getString("Prato") != "") {
                        prato = jsonObject.getString("Prato")
                    }

                    if (jsonObject.getString("Proteína") != "") {
                        proteina = jsonObject.getString("Proteína") + " g"
                    }

                    if (jsonObject.getString("Sal") != "") {
                        sal = jsonObject.getString("Sal") + " g"
                    }

                    if (jsonObject.getString("Sopa") != "") {
                        sopa = jsonObject.getString("Sopa")
                    }

                }
            }
        }catch(e : Exception){
            btn_ementa.isEnabled = false
            btn_ementa.isClickable = false
            btn_ementa.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            println("Host Unreachable")
        }
    }

    private fun getEmenta(){
        val data = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")
        val txt = endereco+"/ementa.json"
        val url = URL(txt+parameter)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject

                    if (jsonObject.getString("Acompanhamento_1") != "") {
                        ac1 = jsonObject.getString("Acompanhamento_1")
                    }

                    if (jsonObject.getString("Acompanhamento_2") != "") {
                        ac2 = jsonObject.getString("Acompanhamento_2")
                    }

                    if (jsonObject.getString("Açúcares") != "") {
                        acucar = jsonObject.getString("Açúcares")
                    }

                    if (jsonObject.getString("Energia") != "") {
                        energia = jsonObject.getString("Energia") + " Kcal"
                    }

                    if (jsonObject.getString("Fibras") != "") {
                        fibras = jsonObject.getString("Fibras") + " g"
                    }

                    if (jsonObject.getString("Hidratos_carbono") != "") {
                        hidratos = jsonObject.getString("Hidratos_carbono") + " g"
                    }

                    if (jsonObject.getString("Lípidos") != "") {
                        lipidos = jsonObject.getString("Lípidos") + " g"
                    }

                    if (jsonObject.getString("Lípidos_saturados") != "") {
                        saturados = jsonObject.getString("Lípidos_saturados") + " g"
                    }

                    if (jsonObject.getString("Prato") != "") {
                        prato = jsonObject.getString("Prato")
                    }

                    if (jsonObject.getString("Proteína") != "") {
                        proteina = jsonObject.getString("Proteína") + " g"
                    }

                    if (jsonObject.getString("Sal") != "") {
                        sal = jsonObject.getString("Sal") + " g"
                    }

                    if (jsonObject.getString("Sopa") != "") {
                        sopa = jsonObject.getString("Sopa")
                    }
                }
            }
        } catch (e : Exception){
            println("Host Unreachable")
        }
    }


    private fun getEmentaVegan(){
        val data = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")
        val txt = endereco + "/ementa_vegan.json"
        val url = URL(txt+parameter)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject

                    if (jsonObject.getString("Acompanhamento_1_Vegan") != "") {
                        ac1_vegan = jsonObject.getString("Acompanhamento_1_Vegan")
                    }

                    if (jsonObject.getString("Acompanhamento_2_Vegan") != "") {
                        ac2_vegan = jsonObject.getString("Acompanhamento_2_Vegan")
                    }

                    if (jsonObject.getString("Açúcares_Vegan") != "") {
                        acucar_vegan = jsonObject.getString("Açúcares_Vegan")
                    }

                    if (jsonObject.getString("Energia_Vegan") != "") {
                        energia_vegan = jsonObject.getString("Energia_Vegan") + " Kcal"
                    }

                    if (jsonObject.getString("Fibras_Vegan") != "") {
                        fibras_vegan = jsonObject.getString("Fibras_Vegan") + " g"
                    }

                    if (jsonObject.getString("Hidratos_carbono_Vegan") != "") {
                        hidratos_vegan = jsonObject.getString("Hidratos_carbono_Vegan") + " g"
                    }

                    if (jsonObject.getString("Lípidos_Vegan") != "") {
                        lipidos_vegan = jsonObject.getString("Lípidos_Vegan") + " g"
                    }

                    if (jsonObject.getString("Lípidos_saturados_Vegan") != "") {
                        saturados_vegan = jsonObject.getString("Lípidos_saturados_Vegan") + " g"
                    }

                    if (jsonObject.getString("Prato_Vegan") != "") {
                        prato_vegan = jsonObject.getString("Prato_Vegan")
                    }

                    if (jsonObject.getString("Proteína_Vegan") != "") {
                        proteina_vegan = jsonObject.getString("Proteína_Vegan") + " g"
                    }

                    if (jsonObject.getString("Sal_Vegan") != "") {
                        sal_vegan = jsonObject.getString("Sal_Vegan") + " g"
                    }

                    if (jsonObject.getString("Sopa_Vegan") != "") {
                        sopa_vegan = jsonObject.getString("Sopa_Vegan")
                    }
                }
            }
        } catch (e : Exception){
            println("Host Unreachable")
        }
    }

    private fun getPessoasCantinaNoMomento(){
        val data = "$year-$month-$day $hour:$minute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")

        val txt = endereco+"/data.json"
        val url = URL(txt+parameter)

        try{
            with(url.openConnection() as HttpURLConnection){
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                println("ola")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject
                    pessoas_cantina = jsonObject.getString("estado")
                }

            }
            pessoas_info.text = pessoas_cantina
        } catch (e : Exception){
            btn_timePicker.isEnabled = false
            btn_timePicker.isClickable = false
            btn_timePicker.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            println("Host Unreachable")
        }
    }

    private fun getPessoasCantina(){

        val data = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"
        val parameter = "?" + URLEncoder.encode("target", "UTF-8") + "=" + URLEncoder.encode(data,"UTF-8")

        val txt = endereco+"/data.json"
        val url = URL(txt+parameter)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject
                    pessoas_cantina = jsonObject.getString("estado")
                }

            }
        } catch (e : Exception){
            println("Host Unreachble")
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()
        TimePickerDialog(this, this, hour, minute, true).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        savedHour = hour
        savedMinute = minute

        val day_text = String.format("%02d", savedDay)
        val month_text = String.format("%02d", savedMonth)
        val hour_text = String.format("%02d", savedHour)
        val minute_text = String.format("%02d", savedMinute)

        tv_textTime.text = "$day_text-$month_text-$savedYear $hour_text:$minute_text"

        getPessoasCantina()
        getEmenta()
        getEmentaVegan()
        pessoas_info.text = pessoas_cantina
    }

    private fun verEmenta(){
        btn_ementa.setOnClickListener {
            val atividade_ementa = Intent(this, EmentaActivity::class.java)
            atividade_ementa.putExtra("ac1", ac1)
            atividade_ementa.putExtra("ac2", ac2)
            atividade_ementa.putExtra("acucar", acucar)
            atividade_ementa.putExtra("energia", energia)
            atividade_ementa.putExtra("fibras", fibras)
            atividade_ementa.putExtra("hidratos", hidratos)
            atividade_ementa.putExtra("lipidos", lipidos)
            atividade_ementa.putExtra("saturados", saturados)
            atividade_ementa.putExtra("prato", prato)
            atividade_ementa.putExtra("proteina", proteina)
            atividade_ementa.putExtra("sal", sal)
            atividade_ementa.putExtra("sopa", sopa)

            if (intent.getStringExtra("data") == null) {
                val day_text = String.format("%02d", day)
                val month_text = String.format("%02d", month)
                val hour_text = String.format("%02d", hour)
                val minute_text = String.format("%02d", minute)

                val data = "$day_text-$month_text-$year $hour_text:$minute_text:00"
                atividade_ementa.putExtra("data", data)
            } else {
                val day_text = String.format("%02d", savedDay)
                val month_text = String.format("%02d", savedMonth)
                val hour_text = String.format("%02d", savedHour)
                val minute_text = String.format("%02d", savedMinute)

                val data = "$day_text-$month_text-$savedYear $hour_text:$minute_text:00"
                atividade_ementa.putExtra("data", data)
            }

            atividade_ementa.putExtra("pessoas_cantina", pessoas_cantina)
            startActivity(atividade_ementa)
        }
    }

    private fun verEmentaVegan(){
        btn_ementa_vegan.setOnClickListener {
            val atividade_ementa = Intent(this, EmentaVeganActivity::class.java)
            atividade_ementa.putExtra("ac1_vegan", ac1_vegan)
            atividade_ementa.putExtra("ac2_vegan", ac2_vegan)
            atividade_ementa.putExtra("acucar_vegan", acucar_vegan)
            atividade_ementa.putExtra("energia_vegan", energia_vegan)
            atividade_ementa.putExtra("fibras_vegan", fibras_vegan)
            atividade_ementa.putExtra("hidratos_vegan", hidratos_vegan)
            atividade_ementa.putExtra("lipidos_vegan", lipidos_vegan)
            atividade_ementa.putExtra("saturados_vegan", saturados_vegan)
            atividade_ementa.putExtra("prato_vegan", prato_vegan)
            atividade_ementa.putExtra("proteina_vegan", proteina_vegan)
            atividade_ementa.putExtra("sal_vegan", sal_vegan)
            atividade_ementa.putExtra("sopa_vegan", sopa_vegan)

            if (intent.getStringExtra("data") == null) {
                val day_text = String.format("%02d", day)
                val month_text = String.format("%02d", month)
                val hour_text = String.format("%02d", hour)
                val minute_text = String.format("%02d", minute)

                val data = "$day_text-$month_text-$year $hour_text:$minute_text:00"
                atividade_ementa.putExtra("data", data)
            } else {
                val day_text = String.format("%02d", savedDay)
                val month_text = String.format("%02d", savedMonth)
                val hour_text = String.format("%02d", savedHour)
                val minute_text = String.format("%02d", savedMinute)

                val data = "$day_text-$month_text-$savedYear $hour_text:$minute_text:00"
                atividade_ementa.putExtra("data", data)
            }

            atividade_ementa.putExtra("pessoas_cantina", pessoas_cantina)
            startActivity(atividade_ementa)
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "MainActivity.action.ACTION_GEOFENCE_EVENT" //pode não estar bem
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1




