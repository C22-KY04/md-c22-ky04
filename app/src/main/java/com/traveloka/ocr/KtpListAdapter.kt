package com.traveloka.ocr

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class KtpListAdapter(private val ktpList: ArrayList<DataItem>) : RecyclerView.Adapter<KtpListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgKtp: ImageView = view.findViewById(R.id.img_ktp)
        private val phName: TextView = view.findViewById(R.id.ph_name)
        private val phNik: TextView = view.findViewById(R.id.ph_nik)

        fun bind(ktp: DataItem){
            Glide.with(itemView.context)
                .load(ktp.attachment)
                .into(imgKtp)
            phName.text = ktp.name
            phNik.text = ktp.idNumber

            itemView.setOnClickListener{
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra("EXTRA_DETAIL", ktp)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ktpList[position])
    }

    override fun getItemCount(): Int = ktpList.size

}