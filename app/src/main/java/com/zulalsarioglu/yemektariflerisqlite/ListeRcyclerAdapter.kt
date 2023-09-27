package com.zulalsarioglu.yemektariflerisqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListeRcyclerAdapter(val yemekListesi : ArrayList<String>, val yemekId : ArrayList<Int>)
    : RecyclerView.Adapter<ListeRcyclerAdapter.YemekHolder>() {
    class YemekHolder (itemView : View ): RecyclerView.ViewHolder(itemView ){
        val recycylerRowText: TextView = itemView.findViewById(R.id.recycyler_row_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
        val inflater = LayoutInflater.from(parent.context )
        val view = inflater.inflate(R.layout.rcycler_row,parent, false)
        return YemekHolder(view)
    }

    override fun getItemCount(): Int {
        return yemekListesi.size
    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {
       holder.recycylerRowText.text = yemekListesi[position]
        holder.itemView.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToTarifFragment("recyclerdangeldim",yemekId[position] )
            Navigation.findNavController(it).navigate(action)
        }
    }

}