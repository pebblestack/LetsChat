package com.example.letschat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        back_to_register_text_view.setOnClickListener {
            finish()
        }

        login_button_login.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.d("LoginActivity", "Sign in not successful!")
                    return@addOnCompleteListener
                }

                Log.d("LoginActivity", "Successfully signed in with uid: ${it.result?.user?.uid}")
//                Toast.makeText(
//                    this,
//                    "Successfully signed in with uid: ${it.result?.user?.uid}",
//                    Toast.LENGTH_LONG
//                ).show()

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("LoginActivity", "Failed to sign in: ${it.message}")
                Toast.makeText(this, "Failed to sign in: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}