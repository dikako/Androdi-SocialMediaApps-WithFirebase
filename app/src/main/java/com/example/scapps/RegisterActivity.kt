package com.example.scapps

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ivImageAkun.setOnClickListener{
            checkPermission()
        }
    }

    val READIMAGE: Int = 253
    fun checkPermission() {

        if (ActivityCompat.checkSelfPermission( this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READIMAGE)
            return
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READIMAGE -> {
                if (grantResults[0] ==
                        PackageManager.PERMISSION_DENIED) {
                    loadImage()
                } else {
                    Toast.makeText(applicationContext, "Gambar tidak dapat diakses!", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    val PICK_IMAGE_CODE = 123
    //Load Gambar
    fun loadImage() {
        var intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK) {
            //Set Photo Profile
            val selectedImage = data.data!!
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage, filePathColum, null, null, null)
            cursor?.moveToFirst()
            val coulumIndex = cursor?.getColumnIndex(filePathColum[0])
            val picturePath = cursor?.getString(coulumIndex?:0)
            cursor?.close()
            ivImageAkun.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    //Simpan Gambar Ke Firebase
    fun SaveImageInFirebase(){
        // Kasih Nama ke gambar yang akan disimpan di firebase
        var currentUser = mAuth!!.currentUser
        val email: String = currentUser!!.email.toString()
        val storage = FirebaseStorage.getInstance();

        //Link dari Firebase Storage
        val storageRef = storage.getReferenceFromUrl("gs://chatapps-48f4a.appspot.com")


        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath = SplitString(email) + "." + df.format(dataobj) + ".jpg"
        val imageRef = storageRef.child("gambar/" + imagePath)
        ivImageAkun.isDrawingCacheEnabled = true
        ivImageAkun.buildDrawingCache()

        //Merubah format dari gambar yanga kan kita save
        val ivDrawable = ivImageAkun.drawable as BitmapDrawable
        val bitmap = ivDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        var addss =
            FirebaseStorage.getInstance().equals("downloadTokens")
        var downloadUrl =
            "https:firebasestorage.googleapis.com/v0/b/" +
                    "chatapps-48f4a.appspot.com/o/gambar%2F" +
                    SplitString(currentUser.email.toString()) + "." + df
                .format(dataobj) + ".jpg" + "?alt=media&token" +
                    addss.toString()
        myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
        myRef.child("Users").child(currentUser.uid)

        loadPost()
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext, "Gambar gagal diUpload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { task ->
            var addss =
                FirebaseStorage.getInstance().equals("downloadTokens")
            var DowloadURLz =
                "https:firebasestorage.googleapis.com/v0/b/" +
                        "chatapps-48f4a.appspot.com/o/gambar%2F" +
                        SplitString(currentUser.email.toString()) + "." + df
                    .format(dataobj) + ".jpg" + "?alt=media&token" +
                        addss.toString()
            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid)
            loadPost()
        }
    }

    fun SplitString(email: String): String {
        val split = email.split("@")
        return split[0]
    }

    override fun onStart() {
        super.onStart()
        loadPost()
    }

    fun loadPost() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            // Intent ke mainActivity kita akan
            var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("Email", currentUser.email)
                intent.putExtra("uid", currentUser.uid)
                startActivity(intent)
                finish()
        }
    }

    //button Daftar
    fun btnDaftar(view: View) {
        // Jika Email Tidak Di Isi
        if (etEmailRegister.text.isEmpty()) {
            Toast.makeText(applicationContext, "Email tidak boleh Kosong", Toast.LENGTH_LONG).show()

            //Jika Password Kosong
        } else if (etPasswordRegister.text.isEmpty()) {
            Toast.makeText(applicationContext, "Password tiadk boleh Kosong", Toast.LENGTH_LONG)
                .show()
            //Jika sudah Benar

        } else {
            LoginToFireBase(etEmailRegister.text.toString(),
                etEmailRegister.text.toString())
        }
    }

    fun LoginToFireBase(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Login Sukses", Toast.LENGTH_LONG).show()
                    SaveImageInFirebase()
            } else {
                Toast.makeText(applicationContext, "Login Gagal", Toast.LENGTH_LONG).show()
            }
        }
    }
}
