package com.example.notebook

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.notebook.R
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.utlis.setupDialog

class MainActivity : AppCompatActivity() {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val updateNotesDialog: Dialog by lazy {
        Dialog(this).apply {
            setupDialog(R.layout.update_notes)
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)


        val addCloseImg = updateNotesDialog.findViewById<ImageView>(R.id.closeImg)
        val updateNotesBinding =updateNotesDialog.findViewById<ImageView>(R.id.closeImg)

        addCloseImg.setOnClickListener{updateNotesDialog.dismiss()}
        updateNotesBinding.setOnClickListener{updateNotesDialog.dismiss()}

        mainBinding.addNewNoteBtn.setOnClickListener{
            updateNotesDialog.show()
        }

    }
}
