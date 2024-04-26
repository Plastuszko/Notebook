package com.example.notebook

import NotesAdapter
import android.app.Dialog
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.utlis.setupDialog
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

            // Sprawdzanie, czy notatka o takim tytule już istnieje
            if (Map_of_titles.containsKey(noteTitle)) {
                // Notatka o takim tytule już istnieje, wyświetl odpowiednie powiadomienie
                Toast.makeText(this, "Notatka o takim tytule już istnieje, wybierz inny tytuł", Toast.LENGTH_SHORT).show()
            } else {
                // Notatka o takim tytule nie istnieje, dodaj nową notatkę do bazy danych
                val noteData = hashMapOf(
                    "who" to user_email,
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

                // Aktualizacja listy notatek po dodaniu nowej notatki
                fetchData(user_email)

                // Zamknięcie dialogu po zapisaniu notatki
                updateNotesDialog.dismiss()
            }
        }



        mainBinding.addNewNoteBtn.setOnClickListener{
            updateNotesDialog.show()
        }






    }

    fun fetchData(userEmail: String) {
        Map_of_titles.clear()
        Map_of_notes.clear()
        db.collection("Notes")
            .whereEqualTo("who", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val noteTitle = document.getString("noteTitle")
                    val noteText = document.getString("note")
                    if(Map_of_titles[noteTitle!!].isNullOrEmpty()) {
                        Map_of_titles[noteTitle!!] = noteTitle!!
                        Map_of_notes[noteTitle!!] = noteText!!
                    }

                }
                // Przekazujemy listę notatek do adaptera
                val notesList = Map_of_titles.toList()
                notesAdapter = NotesAdapter(notesList)
                mainBinding.notesList.adapter = notesAdapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


}
