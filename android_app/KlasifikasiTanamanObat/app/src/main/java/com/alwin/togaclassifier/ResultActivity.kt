package com.alwin.togaclassifier

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alwin.togaclassifier.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // atur warna status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        // Terima data dari Intent
        val label = intent.getStringExtra("label")
        val confidence = intent.getFloatExtra("confidence", 0f)
        val imageUriString = intent.getStringExtra("image_uri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        // Tampilkan hasil ke UI
        binding.tvNamaSpesies.text = label ?: "Tidak diketahui"
        binding.tvConfidence.text = "Confidence: ${"%.2f".format(confidence * 100)}%"

        // Jika kamu punya peta khasiat dan cara olah:
        val khasiat = KhasiatDatabase[label] ?: "Tidak tersedia"
        val caraOlah = PengolahanDatabase[label] ?: "Tidak tersedia"
        val latin = LatinDatabase[label] ?: "Tidak tersedia"
        val bagian = BagianDatabase[label] ?: "Tidak diketahui"

        binding.tvKhasiat.text = Html.fromHtml(khasiat, Html.FROM_HTML_MODE_LEGACY)
        binding.tvPengolahan.text = caraOlah
        binding.tvLatinSpesies.text = latin
        binding.tvBagianDimanfaatkan.text = bagian

        // Tampilkan gambar
        if (imageUri != null) {
            binding.imgTanaman.setImageURI(imageUri)
        }

        // Ulangi klasifikasi
        binding.btnCobaLagi.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    // Simulasi database khasiat
    companion object {
        val KhasiatDatabase = mapOf(
            "bunga tapak dara" to "Menurunkan kolesterol, meredakan diare, antioksidan, mengontrol gula darah dan tekanan darah, mendukung terapi kanker, mempercepat penyembuhan luka.",
            "bunga telang" to "Menangkal radikal bebas, meringankan depresi, menurunkan kolesterol dan gula darah, meredakan radang, mempercepat penyembuhan luka, menjaga daya tahan tubuh, meningkatkan fungsi otak, dan menjaga kesehatan kulit serta rambut.",
            "bunga melati" to "Melancarkan pencernaan, menjaga kesehatan jantung, membantu menurunkan berat badan, meningkatkan fungsi otak, serta mengelola kadar gula darah.",
            "bunga rosella" to "Menurunkan tekanan darah, terutama hipertensi ringan dengan kandungan antioksidan dan vitamin C. Juga membantu menurunkan kolesterol dan memperlambat pelepasan hormon penyempit pembuluh darah.",
            "bunga kembang sepatu" to "Kaya antioksidan, membantu melawan peradangan, menurunkan tekanan darah, kolesterol, dan mendukung kesehatan hati. Juga memiliki sifat antibakteri yang dapat mengendalikan jumlah bakteri tertentu. \n<b>Risiko:</b>\n" +
                    "Teh hibiscus dapat menyebabkan alergi pada orang sensitif, berinteraksi dengan obat-obatan, dan berisiko bagi ibu hamil atau yang menyusui. Konsumsi dosis tinggi mungkin menyebabkan kerusakan hati.",
            "bunga kembang tidur" to "Khasiat pengobatan belum dapat dipastikan secara ilmiah, masih memerlukan penelitian lebih lanjut oleh para ahli. Saat ini, pemanfaatan tanaman ini lebih banyak difokuskan sebagai tanaman hijau dan tanaman hias yang memberikan nilai estetika serta fungsi penghijauan.",
            "bunga kenanga" to "Membantu relaksasi dan mengurangi kecemasan, menurunkan tekanan darah dan detak jantung tidak teratur, melawan infeksi jamur. membantu mengatasi asma dan gejala malaria, mencegah kutu rambut dan gigitan serangga.",
            "bunga kecubung" to "Meredakan nyeri otot dan sendi, pembengkakan, serta rasa nyeri pada luka. Juga untuk meredakan asma dan gangguan pernapasan lainnya.\n" +
                    "<b>Risiko:</b>\n" +
                    "Karena kandungan toksiknya, bunga tidak boleh digunakan sebagai obat dalam (diminum atau dimakan) kecuali oleh tenaga ahli yang memahami dosis medis secara presisi.",
            "bunga mawar" to "Memperkuat daya tahan tubuh, meredakan nyeri haid dan iritasi kulit, memperbaiki suasana hati, mengatasi gangguan pencernaan ringan, mengurangi bau badan, menjaga kesehatan kulit agar tampak awet muda, dan mengurangi risiko penyakit kronis seperti jantung dan kanker.",
            "daun sirih" to "Menjaga kesehatan mulut dan gigi, meredakan batuk dan sakit tenggorokan, menyembuhkan luka, meredakan nyeri haid, menurunkan kadar gula darah, serta sebagai antiseptik alami untuk mencegah infeksi.\n" +
                    "<b>Risiko:</b>\n" +
                    "Penggunaan berlebihan atau jangka panjang dapat menyebabkan iritasi mulut, masalah pencernaan, dan berisiko meningkatkan kanker mulut jika dikunyah bersama pinang dan gambir. Wanita hamil dan menyusui disarankan berkonsultasi sebelum penggunaan.\n",
            "daun sambiloto" to "Melindungi kulit dari radikal bebas, mencegah infeksi kulit, mempercepat penyembuhan luka, meringankan gejala alergi kulit (seperti eksim). \n<b>Risiko:</b>\n" +
                    "Jika dikonsumsi berlebihan, bisa menyebabkan sakit kepala, mual, kelelahan, reaksi alergi, bahkan gagal ginjal akut. Tidak disarankan dikonsumsi bersamaan dengan obat medis tanpa pengawasan dokter. Untuk ibu hamil, menyusui, anak-anak, dan penderita autoimun harus berkonsultasi dengan dokter terlebih dahulu.",
            "daun beluntas" to "Mengontrol kadar gula darah, menurunkan kolesterol, melindungi sel dari radikal bebas, bersifat antikanker, mendukung penyembuhan luka.",
            "daun mint" to "Meredakan gejala flu dan pilek, Menyehatkan sistem pencernaan, meningkatkan sistem imun, meningkatkan fungsi otak, membantu menurunkan berat badan, mengatasi bau mulut, mencegah keracunan, meningkatkan kualitas tidur, mengurangi nyeri menyusui, mengurangi gejala asma, meredakan gejala PMS, mengatasi jerawat, mengatasi komedo",
            "daun teh hijau" to "Menurunkan berat badan, mengontrol kadar kolesterol dan gula darah, melindungi sel dari radikal bebas, menjaga kesehatan jantung, menenangkan pikiran, serta membantu menjaga kesehatan kulit dan mulut.",
            "daun kelor" to "Melancarkan ASI, mempercepat penyembuhan luka, antibakteri, melancarkan pencernaan, antiinflamasi, menurunkan gula darah, menjaga kesehatan jantung, antikanker, meningkatkan imunitas, dan menyehatkan mata.",
            "daun jambu biji" to "Menjaga pencernaan, mengurangi nyeri menstruasi, menurunkan gula darah, menjaga kesehatan jantung, dan menghambat pertumbuhan sel kanker",
            "daun kemangi" to "Menyehatkan pencernaan, meredakan peradangan, melawan radikal bebas, mencegah kanker, mempercepat penyembuhan luka, antibakteri, dan menurunkan stres dan kecemasan.",
            "daun pegagan" to "Menjaga kesehatan kulit seperti membantu penyembuhan luka, mencegah jaringan parut dan stretch mark, melembapkan kulit, serta membersihkan dan mengencangkan kulit. Selain itu, juga berkhasiat meningkatkan fungsi kognitif, mengurangi stres, kecemasan, insomnia, dan berpotensi sebagai antibakteri alami.",
            "daun serai" to "Mengurangi kecemasan, meningkatkan kesehatan pencernaan, menurunkan berat badan, melindungi kesehatan jantung, serta meningkatkan pertumbuhan rambut.  Sereh juga membantu mengatasi sakit kepala, ruam kulit, kolesterol tinggi, dan tekanan darah.\n<b>Risiko:</b>\numumnya aman dikonsumsi, mungkin menimbulkan reaksi alergi jika sensitif terhadap tanaman rumput. Wanita hamil dan menyusui sebaiknya berkonsultasi dengan dokter sebelum mengonsumsi sereh.",
            "daun seledri" to "Menurunkan tekanan darah tinggi, melancarkan pencernaan, mengurangi peradangan, menurunkan kadar kolesterol, mencegah kanker.",
            "daun som jawa" to "Meningkatkan stamina, menguatkan daya tahan tubuh, meningkatkan kesuburan pria, efek antioksidan, menurunkan gula darah, membasmi bakteri, menurunkan kolesterol, memelihara kesehatan otak.\n<b>Risiko:</b>\nKonsumsi berlebihan atau lebih lama dari anjuran dapat menyebabkan efek samping diare, payudara nyeri, nafsu makan hilang, gangguan tidur, masalah menstruasi, kepala sakit, dan mual.",
            "daun pandan" to "Meredakan nyeri sendi dan arthritis, mencegah penyakit jantung, mengobati luka bakar ringan, menjaga fungsi penglihatan, menjaga kesehatan kulit, mengontrol kadar gula darah, mencegah penyakit kanker.",
            "daun pepaya" to "Menurunkan risiko kanker, anti malaria, anti bakteri, mencegah demam berdarah, meningkatkan kekebalan tubuh.\n<b>Risiko:</b>\nKonsumsi berlebihan atau pada kondisi tertentu dapat menghasilkan efek samping seperti toksisitas akibat kandungan alkaloid dan papain, iritasi lambung, reaksi alergi seperti ruam dan gatal, serta risiko merangsang kontraksi yang berpotensi berbahaya bagi ibu hamil.",
            "daun kumis kucing" to "Mengatasi infeksi saluran kemih (ISK) melalui efek diuretik dan antibakteri, membantu mengobati gangguan ginjal dengan mencegah pembentukan batu ginjal, meredakan rematik, mengobati gusi bengkak dengan melawan infeksi bakteri, serta membantu mengontrol kadar gula darah.",
            "lidah buaya" to "Meredakan asam lambung, mengatasi sembelit, mengontrol gula darah, mencegah dehidrasi, meredakan jerawat, dan membantu mengatasi kulit kering.\n<b>Risiko:</b>\njika dikonsumsi berlebihan dalam jangka panjang, dapat menimbulkan diare, hipoglikemia, hingga gangguan fungsi organ."
        )

        val PengolahanDatabase = mapOf(
            "bunga tapak dara" to "• Diseduh seperti teh dari bunga kering yang ditumbuk halus\n"+
                    "atau ditumbuk segar untuk obat luar luka.",
            "bunga telang" to "• Seduh bunga kering sebagai teh (bisa ditambah madu/lemon)\n• diolah jadi sirup, bubuk, es krim, masker, atau pewarna makanan alami",
            "bunga melati" to "• Dapat diolah menjadi teh herbal atau digunakan sebagai campuran makanan seperti kue, puding.",
            "bunga rosella" to "• Seduh bunga rosella kering dalam air mendidih, sajikan hangat atau dingin. \n"+
                    "• Bisa juga dikeringkan dan disimpan untuk digunakan nanti.",
            "bunga kembang sepatu" to "• Seduh kelopak kembang sepatu kering dalam air panas, lalu nikmati teh hibiscus yang bisa disajikan panas atau dingin.",
            "bunga kembang tidur" to "-",
            "bunga kenanga" to "• Minyak atsiri: Rendam bunga dalam minyak kelapa/zaitun, panaskan perlahan (tidak mendidih), saring. Gunakan untuk pijat atau aromaterapi.\n" +
                    "• Air rendaman: Seduh bunga dalam air panas, diamkan, saring. Gunakan untuk cuci wajah atau toner alami.",
            "bunga kecubung" to "• Pengasapan bunga kering: Bunga dikeringkan, dibakar sedikit untuk menghasilkan asap, lalu asap dihirup untuk mengatasi asma atau sesak napas.\n" +
                    "• Kompres atau balur luar: Bunga dilayukan dengan panas atau diolah menjadi salep tradisional, digunakan untuk mengompres bagian tubuh yang sakit.",
            "bunga mawar" to "• Teh mawar: Rebus kelopak segar yang telah dicuci (2 cangkir) dengan 3 cangkir air selama 5 menit, atau seduh 1 sdm kelopak kering dengan air panas selama 10–20 menit. Tambahkan madu jika perlu.\n" +
                    "• Air mawar: Rebus 3 cangkir kelopak dengan 500 ml air hingga warna memudar. Dinginkan dan saring. Gunakan sebagai toner wajah, penyegar kulit, atau obat luka ringan.",
            "daun sirih" to "• Air rebusan untuk berkumur atau diminum: Rebus beberapa lembar daun sirih dalam air hingga mendidih, saring, lalu gunakan untuk berkumur atau minum seperti teh herbal.\n" +
                    "• Luka luar: Haluskan daun sirih segar dan tempelkan langsung pada luka kecil untuk mempercepat penyembuhan.\n" +
                    "• Dioleskan: Oleskan ekstrak daun sirih pada kulit yang gatal atau iritasi akibat gigitan serangga.\n",
            "daun sambiloto" to "• Sebagai minuman: Rebus 5–6 lembar daun sambiloto dengan ½ sdt biji jintan dalam air selama 5–6 menit, saring, dan minum.\n" +
                    "• Sebagai masker kulit: Haluskan daun sambiloto, campur dengan air, oleskan ke kulit selama 10 menit, lalu bilas.\n",
            "daun beluntas" to "• Rebus beberapa lembar daun beluntas dalam 300 ml air dan diamkan hingga sejuk. Air rebusan dikonsumsi dua sampai tiga kali sehari sebelum atau sesudah makan.",
            "daun mint" to "• Teh daun mint: Masukkan 6 lembar daun mint segar (dicuci dan dicincang) ke dalam 150 ml air mendidih. Diamkan 5–10 menit, saring, dan tambahkan madu (opsional). Minum 3–4 kali sehari untuk hasil optimal. \n" +
                    "• Ditumbuk: Tumbukan daun dioleskan untuk jerawat, nyeri menyusui, atau komedo.\n" +
                    "• Dikunyah: Untuk mengatasi bau mulut, cukup kunyah beberapa lembar daun mint segar setelah makan",
            "daun teh hijau" to "• Seduh beberapa lembar daun teh hijau segar dengan air panas bersuhu 80–90°C, diamkan hingga hangat, lalu minum tanpa campuran. Dapat dikonsumsi 3–4 kali sehari setelah makan untuk hasil optimal.",
            "daun kelor" to "• Daun kelor dapat dimasak sayur bening, ditumis, atau di jus. \n"+
                    "• Daun juga dapat dikeringkan, dihaluskan menjadi bubuk, lalu dicampur ke minuman smoothie atau yoghurt.",
            "daun jambu biji" to "• Diolah menjadi teh daun jambu biji, rebus daun jambu biji dalam air selama 10-15 menit, saring, dan minum selagi hangat.",
            "daun kemangi" to "• Dikonsumsi langsung sebagai lalapan segar,  campuran masakan, atau direbus untuk dijadikan air seduhan/herbal (teh kemangi).",
            "daun pegagan" to "• Teh herbal: Daun pegagan diseduh dengan air panas untuk diminum.\n" +
                    "• Penggunaan luar: Daun pegagan diremukkan dan dioleskan langsung ke kulit.",
            "daun serai" to "• Teh Sereh: Potong batang dan daun sereh, rebus dalam air selama 10-15 menit, saring, dan minum selagi hangat.\n"+
                    "• Minyak Sereh: Hancurkan batang sereh, rendam dalam minyak kelapa atau zaitun beberapa hari untuk menghasilkan minyak esensial alami.\n" +
                    "• Bumbu Masakan: Geprek batang dan daun sereh dan masukkan ke masakan seperti sup, kari, atau tumisan.",
            "daun seledri" to "• Jus Seledri: Potong kecil daun seledri, tambahkan bahan lain seperti pir, nanas, perasan lemon, dan es batu. Blender halus, saring, dan diminum.\n" +
                    "• Bumbu Masakan: Daun seledri sebagai pelengkap sup, tumisan, atau salad.",
            "daun som jawa" to "• Direbus untuk diminum sebagai teh atau air rebusan.\n"+
                    "• juga dapat diolah menjadi sup, tumisan, atau lalapan.\n"+
                    "• Disarankan untuk dikonsumsi paling lama 12 minggu.",
            "daun pandan" to "• Direbus: Cuci bersih daun pandan dan buang bagian bawah yang putih. Ikat daun berbentuk simpul, rebus dalam air mendidih 5-10 menit. Kemudian saring dan dinginkan. Air rebusan diminum langsung atau dicampurkan ke teh.\n" +
                    "• Dikeringkan: Keringkan, haluskan daun pandan menjadi bubuk, lalu taburkan pada kulit terbakar.\n",
            "daun pepaya" to "• Direbus: Rebus daun pepaya untuk mengurangi pahit, lalu cuci dengan air dingin dan dimasak untuk dijadikan sayur, dapat dicampur dengan bawang putih dan kelapa parut.\n" +
                    "• Dijadikan Teh: Daun pepaya kering bisa diseduh sebagai teh untuk meningkatkan sistem kekebalan tubuh.\n" +
                    "• Jus Daun Pepaya: Daun pepaya yang sudah direbus bisa dijus dan dikonsumsi.\n",
            "daun kumis kucing" to "Dapat direbus dengan cara mengambil 4–5 lembar daun kumis kucing yang telah dicuci bersih, lalu direbus dalam segelas air hingga mendidih dan dikonsumsi sebagai teh herbal. Jika diinginkan, madu bisa ditambahkan sebagai pemanis alami.",
            "lidah buaya" to "Diolah menjadi jus dengan mengambil gel dari daun segar, lalu diblender bersama air hingga halus, kemudian diminum secara langsung atau dicampur ke dalam smoothies. Untuk menambah rasa, bisa ditambahkan madu, lemon, atau jahe. "
        )

        val LatinDatabase = mapOf(
            "bunga tapak dara" to "(Catharanthus roseus)",
            "bunga telang" to "(Clitoria ternatae)",
            "bunga melati" to "(Jasminum sambac)",
            "bunga rosella" to "(Hibiscus sabdrariffa)",
            "bunga kembang sepatu" to "(Hibiscus rosa-sinensis)",
            "bunga kembang tidur" to "(Malvaviscus penduliflorus)",
            "bunga kenanga" to "(Cananga odorata)",
            "bunga kecubung" to "(Datura sp.)",
            "bunga mawar" to "(Rosa sp.)",
            "daun sirih" to "(Piper betle)",
            "daun sambiloto" to "(Andrographis paniculata)",
            "daun beluntas" to "(Pluchea indica)",
            "daun mint" to "(Mentha sp.)",
            "daun teh hijau" to "(Camellia sinensis)",
            "daun kelor" to "(Moringa oleifera)",
            "daun jambu biji" to "(Psidium guajava)",
            "daun kemangi" to "(Ocimum sanctum)",
            "daun pegagan" to "(Centella Asiatica)",
            "daun serai" to "(Cymbopogon citratus)",
            "daun seledri" to "(Apium graveolens)",
            "daun som jawa" to "(Talinum paniculatum)",
            "daun pandan" to "(Pandanus amaryllifolius)",
            "daun pepaya" to "(Carica papaya)",
            "daun kumis kucing" to "(Orthosiphon aristatus)",
            "lidah buaya" to "(Aloe vera)"
        )

        val BagianDatabase = mapOf(
            "bunga tapak dara" to "Bunga",
            "bunga telang" to "Bunga",
            "bunga melati" to "Bunga",
            "bunga rosella" to "Bunga",
            "bunga kembang sepatu" to "Bunga",
            "bunga kembang tidur" to "-",
            "bunga kenanga" to "Bunga",
            "bunga kecubung" to "Bunga",
            "bunga mawar" to "Bunga",
            "daun sirih" to "Daun",
            "daun sambiloto" to "Daun",
            "daun beluntas" to "Daun",
            "daun mint" to "Daun",
            "daun teh hijau" to "Daun",
            "daun kelor" to "Daun",
            "daun jambu biji" to "Daun",
            "daun kemangi" to "Daun",
            "daun pegagan" to "Daun",
            "daun serai" to "Daun",
            "daun seledri" to "Daun dan batang",
            "daun som jawa" to "Daun",
            "daun pandan" to "Daun",
            "daun pepaya" to "Daun",
            "daun kumis kucing" to "Daun",
            "lidah buaya" to "Daun"
        )
    }
}