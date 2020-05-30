package minorproject.two.leafblightdetection;


import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.provider.MediaStore
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import kotlinx.android.synthetic.main.app_bar_main3.*
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
private lateinit var mCategorization: Categorization
private lateinit var mBitmap: Bitmap
private val mCameraRequestCode = 0
private val mGalleryRequestCode = 2


private val mInputSize = 224
private val mModelPath = "plant_disease_model.tflite"
private val mLabelPath = "plant_labels.txt"





private val mSamplePath = "automn.jpg"
        lateinit var toolbar: Toolbar
        lateinit var drawer: AdvanceDrawerLayout
        lateinit var navigationView: NavigationView
@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            navigationView =
            findViewById<View>(R.id.nav_view) as NavigationView
        toolbar =
        findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer =
        findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout

        drawer.setViewScale(GravityCompat.START, 0.9f) //set height scale for main view (0f to 1f)
        val toggle = ActionBarDrawerToggle(
        this, drawer, toolbar, 0, 0
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        drawer.setViewElevation(
        GravityCompat.START,
        20f
        ) //set main view elevation when drawer open (dimension)

        drawer.setViewScrimColor(
        GravityCompat.START,
        Color.TRANSPARENT
        ) //set drawer overlay coloe (color)

        drawer.setDrawerElevation(GravityCompat.START, 20f) //set drawer elevation (dimension)

        drawer.setContrastThreshold(3f) //set maximum of contrast ratio between white text and background color.

        drawer.setRadius(GravityCompat.START, 25f) //set end container's corner radius (dimension)

        drawer.useCustomBehavior(GravityCompat.START) //assign custom behavior for "Left" drawer

        drawer.useCustomBehavior(GravityCompat.END) //assign custom behavior for "Right" drawer

        //Set Name for user
        //Set Name for user
        val headerView = navigationView.getHeaderView(0)




        mCategorization = Categorization(assets, mModelPath, mLabelPath, mInputSize)

        resources.assets.open(mSamplePath).use {
        mBitmap = BitmapFactory.decodeStream(it)
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
        mPhotoImageView.setImageBitmap(mBitmap)
        }
        bottom_nav.setActiveItem(0);
        val bottomnav: SmoothBottomBar= findViewById<SmoothBottomBar>(R.id.bottom_nav)
        bottomnav.setOnItemSelectedListener(object: OnItemSelectedListener {
        override fun onItemSelect(pos: Int): Boolean {
        when(pos){
        1 -> {
        val intent = Intent(this@MainActivity,Common_Remdies::class.java)
        startActivity(intent)
        return true
        }
        2 -> {
        val intent = Intent(this@MainActivity,About_us::class.java)
        startActivity(intent)
        return true
        }
        }

        return false
        }
        })


        mCameraButton.setOnClickListener {
        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(callCameraIntent, mCameraRequestCode)
        }

        mGalleryButton.setOnClickListener {
        val callGalleryIntent = Intent(Intent.ACTION_PICK)
        callGalleryIntent.type = "image/*"
        startActivityForResult(callGalleryIntent, mGalleryRequestCode)
        }
        mDetectButton.setOnClickListener {
        val progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Wait there I do something...")
        progressDialog.show()
        val handler = Handler()
        handler.postDelayed(Runnable { progressDialog.dismiss()
        val results = mCategorization.recognizeImage(mBitmap).firstOrNull()
        mResultTextView.text= results?.title+"\n Confidence:"+results?.confidence
        }, 2000)

        }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == mCameraRequestCode){
        //Considérons le cas de la caméra annulée
        if(resultCode == Activity.RESULT_OK && data != null) {
        mBitmap = data.extras!!.get("data") as Bitmap
        mBitmap = scaleImage(mBitmap)
        val toast = Toast.makeText(this, ("Image crop to: w= ${mBitmap.width} h= ${mBitmap.height}"), Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM, 0, 20)
        toast.show()
        mPhotoImageView.setImageBitmap(mBitmap)
        mResultTextView.text= "Your photo image set now."
        } else {
        Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show()
        }
        } else if(requestCode == mGalleryRequestCode) {
        if (data != null) {
        val uri = data.data

        try {
        mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        } catch (e: IOException) {
        e.printStackTrace()
        }

        println("Success!!!")
        mBitmap = scaleImage(mBitmap)
        mPhotoImageView.setImageBitmap(mBitmap)

        }
        } else {
        Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show()

        }
        }


        fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
        }

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawer =
        findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout
        when (item.itemId) {

        R.id.about -> {

        val intent = Intent(this@MainActivity,About_us::class.java)
        startActivity(intent)
        }
        R.id.remdy -> {

        val intent = Intent(this@MainActivity,Common_Remdies::class.java)
        startActivity(intent)
        }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true

        }
        override fun onBackPressed() {
        val alertDialogBuilder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Exit Application?")
        alertDialogBuilder
        .setMessage("Click yes to exit!")
        .setCancelable(false)
        .setPositiveButton("Yes",
        DialogInterface.OnClickListener { dialog, id ->
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        System.exit(1)
        })
        .setNegativeButton("No",
        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alertDialog: android.app.AlertDialog? = alertDialogBuilder.create()
        alertDialog?.show()
        }

        }




