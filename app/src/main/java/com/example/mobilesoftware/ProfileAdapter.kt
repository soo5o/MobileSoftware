package com.example.mobilesoftware

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val context: Context?, private val datas: MutableList<ProfileData>) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recycler_ex, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = itemView.findViewById(R.id.tv_rv_name)
        val txtAge: TextView = itemView.findViewById(R.id.tv_rv_age)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]
        holder.txtName.text = data.name
        holder.txtAge.text = data.age.toString()
    }
}
