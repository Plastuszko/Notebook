package com.example.notebook.utlis

import android.app.Dialog
import android.widget.LinearLayout
import java.sql.RowId


fun Dialog.setupDialog(layoutResId: Int){
    setContentView(layoutResId)
    window!!.setLayout(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    setCancelable(false)
}