package com.example.notebook

import NotesAdapter
import android.app.Dialog
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.utlis.setupDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }



    private val updateNotesDialog: Dialog by lazy {
        Dialog(this).apply {
            setupDialog(R.layout.update_notes)
        }

    }

    var Map_of_titles: MutableMap <String,String> = mutableMapOf()
    var Map_of_notes: MutableMap<String,String> = mutableMapOf()
    private lateinit var notesAdapter: NotesAdapter
    var db = Firebase.firestore
    var user_email: String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        fetchData(user_email)


        if(intent.hasExtra("USER")){
            user_email=intent.getStringExtra("USER")!!
            Toast.makeText(this,"Hi, $user_email",Toast.LENGTH_SHORT).show()
        }

        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)




        val saveNotesBtn = updateNotesDialog.findViewById<Button>(R.id.save_notes_btn)
        val addCloseImg = updateNotesDialog.findViewById<ImageView>(R.id.closeImg)
        val updateNotesBinding =updateNotesDialog.findViewById<ImageView>(R.id.closeImg)

        addCloseImg.setOnClickListener{updateNotesDialog.dismiss()}
        updateNotesBinding.setOnClickListener{updateNotesDialog.dismiss()}
        saveNotesBtn.setOnClickListener{
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)

            val noteTitle = ednoteTitle.text.toString()
            val noteText = ednote.text.toString()

            Toast.makeText(this, "Notatka została zapisana: "+ noteText, Toast.LENGTH_SHORT).show()

            val noteData = hashMapOf(
                "who" to user_email, // Tutaj należy wprowadzić odpowiednią wartość dla pola "who"
                "noteTitle" to noteTitle,
                "note" to noteText
            )

            db.collection("Notes")
                .add(noteData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(this, "Notatka została zapisana", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this, "Błąd podczas zapisywania notatki", Toast.LENGTH_SHORT).show()
                }
            fetchData(user_email)
            updateNotesDialog.dismiss()
        }


        mainBinding.addNewNoteBtn.setOnClickListener{
            updateNotesDialog.show()
        }






    }

    fun fetchData(userEmail: String) {
        db.collection("Notes")
            .whereEqualTo("who", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val noteTitle = document.getString("noteTitle")
                    val noteText = document.getString("note")
                    Map_of_titles[noteTitle!!]=noteTitle!!
                    Map_of_notes[noteTitle!!]=noteText!!

                }

                val recyclerView: RecyclerView = mainBinding.notesList
                recyclerView.layoutManager = LinearLayoutManager(this)
                notesAdapter = NotesAdapter(Map_of_titles.toList())
                recyclerView.adapter = notesAdapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)

            }


    }

}
