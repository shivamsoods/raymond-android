package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.adapters.FabricDetailsListAdapter
import com.shivam.raymond.adapters.FabricItemClickListener
import com.shivam.raymond.databinding.FragmentListFabricBinding
import com.shivam.raymond.models.FabricInfoModel
import com.shivam.raymond.models.FabricInfoParcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        if(args.viewType==ScanQrEnum.API_FLOW){
            lifecycleScope.launch(Dispatchers.IO) {
                getFabricApiInfo(args.fabricCode)
            }
        }else{

            getFabricList(args.fabricCode)
        }


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
        val docId = fabricDetailList[itemPosition].documentId!!
        val fabricCode = fabricDetailList[itemPosition].fabricCode

        when (args.viewType) {
            ScanQrEnum.ADD_IMAGE -> {

                val docIds = ArrayList<String>()
                fabricDetailList.forEach {
                    docIds.add(it.documentId!!)
                }


                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToEnterFabricImageFragment(
                        docIds.toTypedArray(), args.fabricCode
                    )
                )

            }
            ScanQrEnum.VIEW_MODIFY_FABRIC_DETAIL -> {
                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToAddViewFabricInfoFragment(
                        docId, "View/Modify Fabric", fabricCode
                    )
                )
            }
            ScanQrEnum.QR_CODE_FLOW -> {
                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToDisplayRackNumberFragment(
                        docId
                    )
                )
            }
            ScanQrEnum.API_FLOW -> {
                val fabricItem=fabricDetailList[itemPosition]

                val payload = FabricInfoParcelable(
                    fabricCode = fabricItem.fabricCode,
                    imageUrl = fabricItem.imageUrl,
                    rackNumber = fabricItem.rackNumber,
                    batch = fabricItem.batch,
                    fileNumber = fabricItem.fileNumber,
                    quantity = fabricItem.quantity
                )

                findNavController().navigate(
                    ListFabricFragmentDirections.actionListFabricFragmentToApiFragment(payload)
                )
            }
        }
    }

    override fun onFabricImageClick(itemPosition: Int) {
        super.onFabricImageClick(itemPosition)

        findNavController().navigate(
            ListFabricFragmentDirections.actionListFabricFragmentToFullScreenImageFragment(
                fabricDetailList[itemPosition].imageUrl!!
            )
        )
    }

    private suspend fun getFabricApiInfo(fabricCode: String) {
        fabricDetailList.clear()

        val fabricResponse = apiService.getFabricInfo(fabricCode)
        if (fabricResponse.isSuccessful && fabricResponse.body() != null) {

            lifecycleScope.launch(Dispatchers.Main) {
                if (fabricResponse.body()!!.isNotEmpty()) {
                    fabricResponse.body()!!.forEach {document->
                        val payload = FabricInfoModel(
                            fabricCode = document.fabricCode,
                            fabricLength = "",
                            fabricWidth = "",
                            imageUrl = document.imageUrl,
                            rackNumber = document.rackNo,
                            batch = document.batch,
                            fileNumber = document.fileNo,
                            documentId = "",
                            quantity = document.quantity
                        )

                        fabricDetailList.add(payload)
                    }

                    setUpRecyclerView(fabricDetailList)

                }
            }
        }

    }
}