package minorproject.two.leafblightdetection

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.widget.Toast
import kotlinx.android.synthetic.main.app_bar_main3.*
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar

class About_us : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        bottom_nav.setActiveItem(2);
       // val actionBar = supportActionBar
        //actionBar!!.title = "About Us"
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "About Us"
        //set back button
        val bottomnav: SmoothBottomBar = findViewById<SmoothBottomBar>(R.id.bottom_nav)
        bottomnav.setOnItemSelectedListener(object: OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                when(pos){
                    0 -> {
                        val intent = Intent(this@About_us,MainActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                    1-> {
                        val intent = Intent(this@About_us,Common_Remdies::class.java)
                        startActivity(intent)
                        return true
                    }

                }

                return false
            }
        })


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
