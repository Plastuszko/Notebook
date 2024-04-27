package com.example.notebook.utlis

import android.app.Dialog
import android.icu.text.CaseMap.Title
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.sql.RowId


fun Dialog.setupDialog(layoutResId: Int){
    setContentView(layoutResId)
    window!!.setLayout(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    setCancelable(false)
}

