package com.ashwin.coolme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(private val appList: List<AppInfo>) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val appCheckbox: CheckBox = view.findViewById(R.id.appCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appName.text = appInfo.name
        holder.appIcon.setImageDrawable(appInfo.icon)
        holder.appCheckbox.isChecked = appInfo.isSelected

        holder.itemView.setOnClickListener {
            appInfo.isSelected = !appInfo.isSelected
            holder.appCheckbox.isChecked = appInfo.isSelected
        }
        
        holder.appCheckbox.setOnClickListener {
            appInfo.isSelected = holder.appCheckbox.isChecked
        }
    }

    override fun getItemCount() = appList.size
    
    fun getSelectedApps(): List<AppInfo> {
        return appList.filter { it.isSelected }
    }
}
