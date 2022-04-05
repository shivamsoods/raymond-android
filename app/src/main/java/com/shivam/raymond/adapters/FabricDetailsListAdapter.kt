package com.shivam.raymond.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shivam.raymond.databinding.FabricListItemBinding
import com.shivam.raymond.models.FabricInfoModel
import timber.log.Timber

class FabricDetailsListAdapter(private var fabricInfoList: List<FabricInfoModel>, private val fabricItemClickListener: FabricItemClickListener) :
    RecyclerView.Adapter<FabricDetailsListAdapter.FabricDetailsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FabricDetailsViewHolder {
        val dataBinding =
            FabricListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return FabricDetailsViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: FabricDetailsViewHolder, position: Int) {
        holder.bind(fabricInfoList[position], fabricItemClickListener, position)
    }

    class FabricDetailsViewHolder(private var dataBinding: FabricListItemBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {

        fun bind(singleFabricItem: FabricInfoModel, fabricItemClickListener: FabricItemClickListener, itemPosition: Int) {
            dataBinding.root.setOnClickListener {
                fabricItemClickListener.onFabricItemClick(itemPosition)
            }
            dataBinding.tvFabricBatch.text = "Batch: ${singleFabricItem.batch}"
            dataBinding.tvFabricCode.text = "Fabric Code: ${singleFabricItem.fabricCode}"
            dataBinding.tvRackNumber.text = "Rack: ${singleFabricItem.rackNumber}"


        }
    }

    override fun getItemCount(): Int {
        return fabricInfoList.size
    }

}

interface FabricItemClickListener {
    fun onFabricItemClick(itemPosition: Int) {}
}



