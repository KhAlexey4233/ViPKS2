package com.finkid.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AppValueEventListener(val result: (DataSnapshot) -> Unit) : ValueEventListener {
  override fun onDataChange(snapshot: DataSnapshot) {
    result(snapshot)
  }

  override fun onCancelled(error: DatabaseError) {
  }
}