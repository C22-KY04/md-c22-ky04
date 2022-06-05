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
        val imgKtp: ImageView = view.findViewById(R.id.img_ktp)
        val phName: TextView = view.findViewById(R.id.ph_name)
        val phNik: TextView = view.findViewById(R.id.ph_nik)

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
//        Glide.with(holder.itemView.context)
//            .load("https://disdukcapil.cilacapkab.go.id/upaaaaa/2022/02/ktp.jpg")
//            .into(holder.imgKtp)
//        holder.tvName.text = "Name"
//        holder.phName.text = ktpList[position].name
//        holder.tvNik.text = "NIK"
//        holder.phNik.text = ktpList[position].nik

//        holder.itemView.setOnClickListener {
//            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
//            intent.putExtra(EXTRADETAIL,  )
//            holder.itemView.context.startActivity(intent)
//        }
    }

    override fun getItemCount(): Int = ktpList.size

}