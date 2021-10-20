package com.albar.firebasestorage

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.albar.firebasestorage.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val imageRef = Firebase.storage.reference

    var curFile: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            curFile = it
            binding.ivImage.setImageURI(it)
        }

        binding.ivImage.setOnClickListener() {
            getImage.launch("image/*")
            editTextVisibility(true)
        }

        binding.btnUpload.setOnClickListener {
            val text = binding.edtImageName.text
            if (text.isEmpty()) {
                Toast.makeText(this@MainActivity, "Field cannot be empty.", Toast.LENGTH_LONG)
                    .show()
            } else {
                closeKeyboard(binding.btnUpload)
                uploadImageToStorage(text.toString())
                editTextVisibility(false)
            }
        }

        binding.btnDownload.setOnClickListener {
            binding.ivImage.clearFocus()
            downloadImage("myImage")
        }

        binding.btnDelete.setOnClickListener {
            deleteImage("myImage")
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Successfully uploaded Image",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$filename").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Image Deleted Successfully", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { // Moving to main thread
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            withContext(Dispatchers.Main)
            {
                Toast.makeText(this@MainActivity, "Download Successfully", Toast.LENGTH_LONG).show()
                binding.ivImage.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { // Moving to main thread
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun editTextVisibility(state: Boolean) {
        if (state) {
            binding.edtImageName.visibility = View.VISIBLE
        } else {
            binding.edtImageName.visibility = View.GONE
        }
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
