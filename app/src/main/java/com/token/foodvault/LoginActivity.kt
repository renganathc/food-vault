package com.token.foodvault

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val login_btn = findViewById<Button>(R.id.login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("458557175304-qj573p7ii6gq7kafkleo37cq65kaqd94.apps.googleusercontent.com")
            .setHostedDomain("student.nitandhra.ac.in")
            .requestEmail()
            .build()

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        login_btn.setOnClickListener{
            signInWithGoogle()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }

    }

    private fun handleResults(task : Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount = task.result
            if (account != null) {
                updateUI(account)
            }
        }
        else {
            Toast.makeText(this, "Login Failed. Try again", Toast.LENGTH_LONG).show()
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        }
    }

    private fun updateUI (account: GoogleSignInAccount) {

        val email = account.email

        if (email != null && email.endsWith("@student.nitandhra.ac.in")) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else {
                    Toast.makeText(this, "Login Failed. Try again.", Toast.LENGTH_LONG).show()
                    GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                }
            }
        }

        else {
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            Toast.makeText(this, "Please use your college student email!", Toast.LENGTH_LONG).show()
        }

    }

}