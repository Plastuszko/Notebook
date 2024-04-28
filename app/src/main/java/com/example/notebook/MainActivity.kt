package com.example.notebook

import NotesAdapter
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R
import com.example.notebook.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), NotesAdapter.OnNoteDeleteListener, NotesAdapter.OnNoteClickListener {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    //↓↓↓ inicjalizacja bazy danych
    private val db = FirebaseFirestore.getInstance()
    private lateinit var notesAdapter: NotesAdapter
    private var user_email: String = ""
    private var Map_of_titles: MutableMap<String, String> = mutableMapOf()
    private var Map_of_notes: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        if (intent.hasExtra("USER")) {
            user_email = intent.getStringExtra("USER")!!
            Toast.makeText(this, "Hi, $user_email", Toast.LENGTH_SHORT).show()
        }

        val updateNotesDialog = Dialog(this).apply {
            setContentView(R.layout.update_notes)
        }

        mainBinding.addNewNoteBtn.setOnClickListener {
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)
            ednote.text=""
            ednoteTitle.text=""
            updateNotesDialog.show()
        }

        val saveNotesBtn = updateNotesDialog.findViewById<Button>(R.id.save_notes_btn)
        val closeNotesBtn= updateNotesDialog.findViewById<ImageView>(R.id.closeImg)

        closeNotesBtn.setOnClickListener{
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)
            ednoteTitle.text=""
            ednote.text=""
            updateNotesDialog.dismiss()

        }

        saveNotesBtn.setOnClickListener {
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)


            val noteTitle = ednoteTitle.text.toString()
            val noteText = ednote.text.toString()

            // Sprawdzanie, czy notatka o takim tytule już istnieje
            if (Map_of_titles.containsKey(noteTitle)) {
                // Notatka o takim tytule już istnieje, daje powiadomienie
                Toast.makeText(this, "Notatka o takim tytule już istnieje, wybierz inny tytuł", Toast.LENGTH_SHORT).show()
            } else {
                //nie ma takiej notatki, można tworzyć
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
                        // Aktualizacja listy notatek po dodaniu nowej notatki
                        lifecycleScope.launch {
                            fetchData(user_email)
                        }
                        ednote.text= noteText
                        Log.d(TAG,"ednote:"+ednote.text.toString())
                        // Zamknięcie dialogu po zapisaniu notatki
                        updateNotesDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                        Toast.makeText(this, "Błąd podczas zapisywania notatki", Toast.LENGTH_SHORT).show()
                    }
            }
            ednote.text=""
            ednoteTitle.text=""
        }
        //↓↓↓ inicjalizacja recyclerView
        val recyclerView: RecyclerView = findViewById(R.id.notes_list)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Pobranie danych przy pierwszym uruchomieniu
        lifecycleScope.launch {
            fetchData(user_email)
        }
    }
    //↓↓↓ funckja odpowiadająca za usunięcie notatki po naciśnięciu obrazka kosza
    override fun onDeleteClicked(noteTitle: String) {
        db.collection("Notes")
            //szukanie notatki  o określonym tytule, którą należy usunąć
            .whereEqualTo("noteTitle", noteTitle)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    //usuwa notatke
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d(TAG,"usunięto")
                            // Usunięto notatkę pomyślnie
                            // Aktualizacja listy notatek po usunięciu
                            lifecycleScope.launch {
                                fetchData(user_email)
                            }
                        }
                        .addOnFailureListener { e ->
                            // Błąd podczas usuwania notatki
                            Log.e(TAG, "Error deleting note", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Błąd podczas pobierania notatek
                Log.e(TAG, "Error getting notes", exception)
            }
    }
    // ↓↓↓ pobieranie aktualnych danych z bazy danych i aktualizowanie listy recycle View
    private suspend fun fetchData(userEmail: String) {
        try {
            val documents = db.collection("Notes")
                .whereEqualTo("who", userEmail)
                .get()
                .await()

            Map_of_titles.clear()
            Map_of_notes.clear()
            for (document in documents) {
                val noteTitle = document.getString("noteTitle")
                val noteText = document.getString("note")
                if (noteTitle != null && noteText != null) {
                    Map_of_titles[noteTitle] = noteTitle
                    Map_of_notes[noteTitle] = noteText
                    Log.d(TAG,"Map of notes1: $Map_of_notes")
                }
            }

            // Przekazujemy listę notatek do adaptera
            val notesList = Map_of_titles.toList()
            notesAdapter = NotesAdapter(notesList, this, this)
            notesAdapter.setOnNoteDeleteListener(this)
            mainBinding.notesList.adapter = notesAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error getting documents: ", e)
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }

    //↓↓↓ funkcja odpowiadająca za działania podczas widoku dialogu edycji notatki
    override fun onNoteClicked(noteTitle: String, noteText: String) {
        val updateNotesDialog = Dialog(this).apply {
            setContentView(R.layout.update_notes)

            // Tutaj znajduje się kod inicjalizujący elementy formularza edycji notatki
            val ednoteTitle = findViewById<TextView>(R.id.ednoteTitle)
            val ednote = findViewById<TextView>(R.id.ednote)
            ednoteTitle.text = noteTitle
            Log.d(TAG, "Map_of_note[notetitle]: "+Map_of_notes[noteTitle].toString())
            ednote.text = Map_of_notes[noteTitle]

            Log.d(TAG, "Note title: $noteTitle")

            show()
        }

        val saveNotesBtn = updateNotesDialog.findViewById<Button>(R.id.save_notes_btn)
        val closeNotesBtn= updateNotesDialog.findViewById<ImageView>(R.id.closeImg)

        //↓↓↓ działania przycisku zapisującego dane w bazie danych
        saveNotesBtn.setOnClickListener {
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)

            val newNoteTitle = ednoteTitle.text.toString()
            val newNoteText = ednote.text.toString()

            // Sprawdź, czy nowy tytuł jest różny od starego tytułu
            if (newNoteTitle != noteTitle || newNoteText != noteText) {
                // Znajdź notatkę o starej nazwie i zaktualizuj ją
                lifecycleScope.launch {
                    db.collection("Notes")
                        .whereEqualTo("noteTitle", noteTitle)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                // Zaktualizuj wartość noteTitle w bazie danych
                                document.reference.update("noteTitle", newNoteTitle)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                                        // Po zaktualizowaniu tytułu, odświeżamy dane w adapterze
                                        lifecycleScope.launch {
                                            fetchData(user_email)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error updating document", e)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents: ", exception)
                        }
                        .await()
                }
            }

            //↓↓↓ Zaktualizuj wartość note w bazie danych
            db.collection("Notes")
                .whereEqualTo("noteTitle", noteTitle)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Zaktualizuj wartość note w bazie danych
                        document.reference.update("note", newNoteText)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot successfully updated!")
                                Map_of_notes[noteTitle] = newNoteText
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error updating document", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

            // Aktualizacja listy notatek po zapisaniu nowej notatki
            lifecycleScope.launch {
                fetchData(user_email)
            }

            // Zamknięcie dialogu po zapisaniu notatki
            updateNotesDialog.dismiss()
        }
        //↓↓↓ wyłączenie okna dialogowego edycji notatek
        closeNotesBtn.setOnClickListener{
            updateNotesDialog.dismiss()
        }
    }
}
