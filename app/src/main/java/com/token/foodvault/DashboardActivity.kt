package com.token.foodvault

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val user_name = user!!.displayName!!.removeSuffix("NITANP").trimEnd()

        val name = findViewById<TextView>(R.id.name)

        name.text = user_name

        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        db.collection("app-data").document("app-data").get()
            .addOnSuccessListener {
                document ->
                if (document.exists()) {
                    val version = document.getString("version")
                    if (version != "1") {
                        Toast.makeText(this, "Please Update App to continue using", Toast.LENGTH_LONG).show()
                        finish()
                        System.exit(0)
                    }
                }
            }

        val open_scanner = findViewById<Button>(R.id.open_scan)

        open_scanner.setOnClickListener{
            val scanner = IntentIntegrator(this)
            scanner.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.initiateScan()
        }

        val help_btn = findViewById<Button>(R.id.help)

        help_btn.setOnClickListener {
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if(result.contents == null) {
                    Toast.makeText(this, "Scan Failed. Try again", Toast.LENGTH_LONG).show()
                }

                else if (result.contents != "kamadhenu-1") {
                    Toast.makeText(this, "Invalid Token", Toast.LENGTH_LONG).show()
                }

                else {
                    val db = FirebaseFirestore.getInstance()
                    db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(false)
                        .build()

                    var user_email = FirebaseAuth.getInstance().currentUser!!.email!!.removeSuffix("@student.nitandhra.ac.in")

                    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                    val data = hashMapOf("latest-token" to date)

                    db.collection("users").document(user_email!!).get()
                        .addOnSuccessListener { document ->
                            Toast.makeText(this, "Processing Token. Please wait", Toast.LENGTH_SHORT).show()
                            if (document.exists()) {
                                val latest_token = document.getString("latest-token")
                                if (latest_token == date) {
                                    Toast.makeText(this, "Token Already Claimed", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("users").document(user_email!!)
                                        .set(data)
                                        .addOnSuccessListener {
                                            val intent = Intent(this, TokenActivity::class.java)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener{
                                            Toast.makeText(this, "Failed. Try again", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                            else {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(user_email!!)
                                    .set(data)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, TokenActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(this, "Failed. Try again", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                }
            }
        }

        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}