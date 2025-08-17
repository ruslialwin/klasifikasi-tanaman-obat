package com.alwin.togaclassifier

import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alwin.togaclassifier.databinding.ActivityPreviewBinding
import com.alwin.togaclassifier.databinding.ActivitySplashBinding
import com.alwin.togaclassifier.ml.ModelConvnextFinalFp16
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.IOException

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var imageView: ImageView
    private lateinit var btnClassify: Button
    private lateinit var btnBack: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // atur warna status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        imageView = binding.imagePreview
        btnClassify = binding.btnClassify
        btnBack = binding.btnBack

        imageUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("imageUri", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("imageUri") as? Uri
        }
        // Tampilkan gambar
        imageUri?.let {
            imageView.setImageURI(it)
        }

        btnClassify.setOnClickListener {
            imageUri?.let {
                runModelInBackground(it)
            } ?: Toast.makeText(this, "Gambar tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }

        // Ulangi klasifikasi
        btnBack.setOnClickListener {
            finish()
        }

        // Aksi tekan lama di gambar, minta izin download
        imageView.setOnLongClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@setOnLongClickListener true
        }
    }

    // Jalankan proses klasifikasi secara asynchronous
    private fun runModelInBackground(uri: Uri) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.loadingOverlay.visibility = View.VISIBLE
            }

            try {
                val (predictedLabel, confidenceScore) = withContext(Dispatchers.Default) {
                    processImage(uri)
                }
                withContext(Dispatchers.Main) {
                    binding.loadingOverlay.visibility = View.GONE

                    if(confidenceScore >= 0.8f){
                        // Kirim hasil ke ResultActivity
                        val intent = Intent(this@PreviewActivity, ResultActivity::class.java)
                        intent.putExtra("label", predictedLabel)
                        intent.putExtra("confidence", confidenceScore)
                        intent.putExtra("image_uri", uri.toString())
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@PreviewActivity,
                            "Model tidak yakin dengan prediksi (confidence < 0.8)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ModelError", "Error in coroutine: ${e.message}")
                Toast.makeText(this@PreviewActivity, "Terjadi kesalahan saat memproses gambar.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Proses gambar untuk klasifikasi menggunakan model TensorFlow Lite
    private fun processImage(uri: Uri): Pair<String, Float>{
        // load tf lite model
        val togaModel = ModelConvnextFinalFp16.newInstance(this)

        val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Gagal membuka URI")
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Resize ke ukuran 224x224
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // Normalisasi manual ke [0.0-1.0]
        val imgArray = FloatArray(224 * 224 * 3)
        var idx = 0
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                if (x == 0 && y == 0) {
                    Log.i("Preprocess", "Pixel(0,0): R=$r, G=$g, B=$b")
                }

                imgArray[idx++] = r
                imgArray[idx++] = g
                imgArray[idx++] = b
            }
        }

        // Buat input TensorBuffer dengan shape dan tipe data sesuai model
        val inputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, 224, 224, 3), DataType.FLOAT32
        )
        inputBuffer.loadArray(imgArray)

        // Masukkan input ke model
        val outputs = togaModel.process(inputBuffer)
        val outputTensorBuffer = outputs.outputFeature0AsTensorBuffer
        val outputArray = outputTensorBuffer.floatArray
        togaModel.close()

        // Ambil label dengan indeks prediksi tertinggi
        val labels = loadLabels()
        val maxIdx = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
        val predictedLabel = if (maxIdx in labels.indices) labels[maxIdx] else "Tidak diketahui"
        val confidenceScore = outputArray[maxIdx]

        Log.i("ModelOutput", "Prediksi: $predictedLabel (score=$confidenceScore)")

        return Pair(predictedLabel, confidenceScore)
    }

    private fun loadLabels(): List<String> {
        return assets.open("labels.txt").bufferedReader().useLines { it.toList() }
    }

    //simpan gambar ke galeri
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
        if (isGranted){
            AlertDialog.Builder(this).setTitle("Download Image?")
                .setMessage("Do you want to download this image to your device?")
                .setPositiveButton("Yes"){_, _ ->
                    val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
                    val bitmap = drawable.bitmap
                    downloadImage(bitmap)
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Toast.makeText(this, "Please allow permission to download image", Toast.LENGTH_LONG).show()
        }
    }

    // Fun that takes a bitmap and store to user's device
    private fun downloadImage(mBitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "TOGA_Images"+System.currentTimeMillis()/1000)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (uri != null){
            contentResolver.insert(uri, contentValues)?.also {
                contentResolver.openOutputStream(it).use { outputStream ->
                    if (!mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)) {
                        throw IOException("Couldn't save the bitmap")
                    }
                    else {
                        Toast.makeText(applicationContext, "Image saved!", Toast.LENGTH_LONG).show()
                    }
                }
                return it
            }
        }
        return null
    }
}