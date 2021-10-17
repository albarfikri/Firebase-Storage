package com.albar.firebasestorage

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.albar.firebasestorage.databinding.ActivityMainBinding

private const val REQUEST_CODE_IMAGE_PICK = 0

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var curFile: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                binding.ivImage.setImageURI(it)
            }
        )

        binding.ivImage.setOnClickListener() {
            getImage.launch("image/*")
        }
    }
}
