package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.adapters.FabricDetailsListAdapter
import com.shivam.raymond.adapters.FabricItemClickListener
import com.shivam.raymond.databinding.FragmentListFabricBinding
import com.shivam.raymond.models.FabricInfoModel
import timber.log.Timber


class ListFabricFragment : BaseFragment(), FabricItemClickListener {
    private lateinit var listFabricBinding: FragmentListFabricBinding
    private val args: ListFabricFragmentArgs by navArgs()
    private lateinit var fabricDetailsListAdapter: FabricDetailsListAdapter
    private val fabricDetailList = ArrayList<FabricInfoModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listFabricBinding = FragmentListFabricBinding.inflate(layoutInflater)
        return listFabricBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getFabricList(args.fabricCode)

    }


    private fun getFabricList(fabricCode: String) {
        fabricDetailList.clear()
        db.collection("fabric")
            .whereEqualTo("fabricCode", fabricCode)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    findNavController().navigateUp()
                } else {
                    it.documents.forEach { document ->
                        val payload = FabricInfoModel(
                            fabricCode = document["fabricCode"].toString(),
                            fabricLength = if (document["fabricLength"] == null) "" else document["fabricLength"].toString(),
                            fabricWidth = if (document["fabricWidth"] == null) "" else document["fabricWidth"].toString(),
                            imageUrl = if (document["imageUrl"] == null) null else document["imageUrl"].toString(),
                            rackNumber = if (document["rackNumber"] == null) "" else document["rackNumber"].toString(),
                            batch = if (document["batch"] == null) "" else document["batch"].toString(),
                            fileNumber = if (document["fileNumber"] == null) "" else document["fileNumber"].toString(),
                            documentId = document.id
                        )

                        fabricDetailList.add(payload)
                    }
                    setUpRecyclerView(fabricDetailList)
                }
            }
            .addOnFailureListener { Timber.e("Failed to fetch Fabric Code") }
    }

    private fun setUpRecyclerView(fabricDetailList: ArrayList<FabricInfoModel>) {
        fabricDetailsListAdapter = FabricDetailsListAdapter(fabricDetailList, this)
        listFabricBinding.rvFabrics.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        listFabricBinding.rvFabrics.adapter = fabricDetailsListAdapter
    }

    override fun onFabricItemClick(itemPosition: Int) {
        super.onFabricItemClick(itemPosition)

        when (args.viewType) {
            ScanQrEnum.ADD_IMAGE -> {

                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToEnterFabricImageFragment(
                        fabricDetailList[itemPosition].documentId!!
                    )
                )

            }
            ScanQrEnum.VIEW_MODIFY_FABRIC_DETAIL -> {
                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToAddViewFabricInfoFragment(
                        fabricDetailList[itemPosition].documentId,
                        "View/Modify Fabric"
                    )
                )
            }
        }
    }
}