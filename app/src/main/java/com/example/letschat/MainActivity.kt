package com.example.letschat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        register_button_register.setOnClickListener {
            performRegistration()
        }

        profile_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedImageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //proceed and check what the selected image was...
            Log.d("MainActivity", "Photo was selected")

            selectedImageUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            profile_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegistration() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d("MainActivity", "Email is $email")
        Log.d("MainActivity", "Password is $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_LONG).show()
            return
        }

        //Firebase authentication to create a user with email/password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Log.d("MainActivity", "Registration not successful")
                        return@addOnCompleteListener
                    }
                    //else if successful
                    Log.d("MainActivity", "Successfully created user with uid: ${it.result?.user?.uid}")
                    Toast.makeText(
                            this,
                            "Successfully created user.",
                            Toast.LENGTH_LONG
                    ).show()

                    uploadImageToFirebase()
                }
                .addOnFailureListener {
                    Log.d("MainActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG)
                            .show()
                }
    }

    private fun uploadImageToFirebase() {
        if (selectedImageUri == null) {
            Log.d("MainActivity", "Photo was null")
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")

        ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Successfully uploaded image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("MainActivity", "File location is: $it")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d("MainActivity", "Failed to upload image: ${it.message}")
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Saved user to firebase database")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Log.d("MainActivity", "Failed to save user to database: ${it.message}")
                }
    }
}

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String): Parcelable {
    constructor() : this("", "", "")
}