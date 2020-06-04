package it.polito.mad.mhackeroni.view


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.BoughtItemAdapter
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.viewmodel.BoughtItemsListFragmentViewModel
import org.json.JSONArray


class BoughtItemsListFragment: Fragment() {

    private lateinit var myAdapter: BoughtItemAdapter
    private lateinit var vm : BoughtItemsListFragmentViewModel
    private lateinit var itemList: RecyclerView
    private var profile : MutableLiveData<Profile> = MutableLiveData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       return inflater.inflate(R.layout.fragment_itembought_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseRepo.INSTANCE.updateToken(FirebaseRepo.INSTANCE.getID(requireContext()))

        vm = ViewModelProvider(this).get(BoughtItemsListFragmentViewModel::class.java)
        vm.uid = FirebaseRepo.INSTANCE.getID(requireContext())

        itemList = view.findViewById(R.id.itembought_list)

        myAdapter =
            BoughtItemAdapter(
                mutableListOf(),
                object : BoughtItemAdapter.MyAdapterListener {
                    override fun itemViewOnClick(item: Item) {
                        navigateWithInfo(item)
                    }

                    override fun ratingItemOnClick(item: Item) {
                        showFeedbackDialog(item)
                    }
                })

        itemList.adapter = myAdapter
        itemList.layoutManager = LinearLayoutManager(context)

        vm.getBoughtItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
        })
    }

    private fun navigateWithInfo(item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        bundle.putBoolean("allowModify", false)
        view?.findNavController()?.navigate(R.id.action_nav_boughtItemsList_to_nav_ItemDetail, bundle)
    }


    private fun showFeedbackDialog(item: Item) {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.feedback_dialog_box)

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val cancelBtn = dialog.findViewById<Button>(R.id.feedback_cancel_btn)
        val okBtn = dialog.findViewById<Button>(R.id.feedback_ok_btn)

        val comment = dialog.findViewById<EditText>(R.id.comment)

        var newRating: Int = 0

        val star1 = dialog.findViewById<ImageView>(R.id.star1)
        val star2 = dialog.findViewById<ImageView>(R.id.star2)
        val star3 = dialog.findViewById<ImageView>(R.id.star3)
        val star4 = dialog.findViewById<ImageView>(R.id.star4)
        val star5 = dialog.findViewById<ImageView>(R.id.star5)

        star1.setOnClickListener {
            star1.background = resources.getDrawable(R.drawable.ic_star)
            star2.background = resources.getDrawable(R.drawable.ic_emptystar)
            star3.background = resources.getDrawable(R.drawable.ic_emptystar)
            star4.background = resources.getDrawable(R.drawable.ic_emptystar)
            star5.background = resources.getDrawable(R.drawable.ic_emptystar)
            newRating = 1
        }
        star2.setOnClickListener {
            star1.background = resources.getDrawable(R.drawable.ic_star)
            star2.background = resources.getDrawable(R.drawable.ic_star)
            star3.background = resources.getDrawable(R.drawable.ic_emptystar)
            star4.background = resources.getDrawable(R.drawable.ic_emptystar)
            star5.background = resources.getDrawable(R.drawable.ic_emptystar)
            newRating = 2
        }
        star3.setOnClickListener {
            star1.background = resources.getDrawable(R.drawable.ic_star)
            star2.background = resources.getDrawable(R.drawable.ic_star)
            star3.background = resources.getDrawable(R.drawable.ic_star)
            star4.background = resources.getDrawable(R.drawable.ic_emptystar)
            star5.background = resources.getDrawable(R.drawable.ic_emptystar)
            newRating = 3
        }
        star4.setOnClickListener {
            star1.background = resources.getDrawable(R.drawable.ic_star)
            star2.background = resources.getDrawable(R.drawable.ic_star)
            star3.background = resources.getDrawable(R.drawable.ic_star)
            star4.background = resources.getDrawable(R.drawable.ic_star)
            star5.background = resources.getDrawable(R.drawable.ic_emptystar)
            newRating = 4
        }
        star5.setOnClickListener {
            star1.background = resources.getDrawable(R.drawable.ic_star)
            star2.background = resources.getDrawable(R.drawable.ic_star)
            star3.background = resources.getDrawable(R.drawable.ic_star)
            star4.background = resources.getDrawable(R.drawable.ic_star)
            star5.background = resources.getDrawable(R.drawable.ic_star)
            newRating = 5
        }

        okBtn.setOnClickListener {
            if(newRating!=0) {
                val repo = FirebaseRepo.INSTANCE

                repo.getProfileRef(item.user).get().addOnCompleteListener { task ->
                    profile.value = Profile()

                    if (task.isSuccessful) {
                        if (task.result?.exists()!!) {
                            profile.value = task?.result?.toObject(Profile::class.java)

                            profile.value?.totRating = profile.value?.totRating?.plus(newRating)!!
                            profile.value?.numRating = profile.value?.numRating?.plus(1)!!

                            var feedbacks: ArrayList<String>? = profile.value?.feedbacks

                            if (!comment.text.isNullOrEmpty()) {
                                feedbacks?.add("${item.name}:${newRating}-${comment.text}")
                            }
                            else{
                                feedbacks?.add("${item.name}:${newRating}-null")
                            }

                            FirebaseRepo.INSTANCE.updateRating(
                                item.user,
                                profile.value?.totRating!!,
                                profile.value?.numRating!!
                            )

                            if (feedbacks != null) {
                                for (i in feedbacks) {
                                    FirebaseRepo.INSTANCE.updateFeedback(item.user, i)
                                }
                            }

                            FirebaseRepo.INSTANCE.insertFeedback(item, true)
                            view?.let { it1 ->
                                Snackbar.make(
                                    it1,
                                    R.string.feedback_done,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            dialog.dismiss()

                        }
                    }

                }
            }else
                view?.let { it1 -> Snackbar.make(it1,R.string.feedback_not_found, Snackbar.LENGTH_SHORT).show() }
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
