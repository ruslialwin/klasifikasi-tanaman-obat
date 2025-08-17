package com.alwin.togaclassifier

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alwin.togaclassifier.databinding.ActivityMainBinding
import com.alwin.togaclassifier.ml.ModelConvnextFinalFp16
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // Deklarasi variabel untuk view binding dan elemen UI
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private val galleryRequestCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // atur warna status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        // Menghubungkan komponen layout dengan variabel
        imageView = binding.imageView
        button = binding.btnCaptureImage
        val buttonLoad = binding.btnLoadImage

        // Aksi ketika tombol kamera ditekan
        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicturePreview.launch(null) // buka kamera
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA) // minta izin kamera
            }
        }
        // Aksi ketika tombol ambil dari galeri ditekan
        buttonLoad.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                // Buka galeri untuk memilih gambar
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            } else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Minta izin kamera atau galeri
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if (granted){
            takePicturePreview.launch(null)
        } else {
            Toast.makeText(this, "Permission denied!! Try again", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher kamera (ambil gambar)
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap->
        if(bitmap != null){
            imageView.setImageBitmap(bitmap)

            // Simpan ke galeri sementara, lalu kirim Uri-nya
            val uri = saveImageToCache(bitmap)
            if (uri != null) {
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra("imageUri", uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher ambil gambar dari galeri
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultReceived(galleryRequestCode, result)
    }

    // Tangani hasil dari galeri
    private fun onResultReceived(requestCode: Int, result: ActivityResult?){
        when(requestCode){
            galleryRequestCode ->{
                if (result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let { uri ->
                        Log.i("TAG", "onResultReceived. $uri")
                        val bitmap =
                            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)

                        val intent = Intent(this, PreviewActivity::class.java)
                        intent.putExtra("imageUri", uri)
                        startActivity(intent)
                    }
                } else {
                    Log.e("TAG", "onActivityResult: error in selecting image")
                }
            }
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        val filename = "temp_image_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/TOGA")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
        return uri
    }

    // Reset UI saat kembali dari halaman lain
    override fun onResume() {
        super.onResume()
        resetUI()
    }

    private fun resetUI() {
        binding.imageView.setImageResource(R.drawable.placeholder)
    }
}