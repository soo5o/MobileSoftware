package com.example.mobilesoftware

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FieldValue

class RecordDialogFragment : DialogFragment() {
    lateinit var csBtn: Button
    lateinit var saveBtn: Button
    lateinit var recordText: EditText
    private lateinit var fname: String

    companion object {
        private const val ARG_DATA = "fname"
        fun newInstance(data: String): RecordDialogFragment {
            val fragment = RecordDialogFragment()
            val args = Bundle()
            args.putString(ARG_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        csBtn = view.findViewById(R.id.cancelBtn)
        saveBtn = view.findViewById(R.id.saveBtn)
        recordText = view.findViewById(R.id.inputRecord)
        fname = arguments?.getString(ARG_DATA).toString() //filename: uid+date
        MyApplication.db.collection("record")
            .document("$fname")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val content = document.data?.get("content").toString()
                    if (content != "null") {
                        recordText.setText(content)
                    }
                } else {
                    Log.d("runTo", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("runTo", "get failed with ", exception)
            }
        saveBtn.setOnClickListener {
            if (recordText.text.toString().isEmpty()) {
                MyApplication.db.collection("record")
                    .document("$fname")
                    .update(
                        mapOf(
                            "content" to FieldValue.delete()
                        )
                    )
            }
            saveStore()
            dismiss() // 다이얼로그를 닫습니다.
        }
        // 닫기 버튼에 대한 클릭 리스너 설정
        csBtn.setOnClickListener {
            dismiss() // 다이얼로그를 닫습니다.
        }
    }

    private fun saveStore() {
        //add............................
        val data = mapOf(
            "content" to recordText.text.toString()
        )
        MyApplication.db.collection("record")
            .document("$fname")
            .set(data)
            .addOnSuccessListener {
                Log.d("runTo", "record data save successful")
            }
            .addOnFailureListener {
                Log.d("runTo", "record data save error", it)
            }
    }
}