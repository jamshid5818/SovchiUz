package uz.ibrohim.sovchiuz.read_data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appuj.customizedalertdialoglib.CustomizedAlertDialog
import com.appuj.customizedalertdialoglib.CustomizedAlertDialogCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import uz.ibrohim.sovchiuz.R
import uz.ibrohim.sovchiuz.databinding.FragmentMaleBinding
import uz.ibrohim.sovchiuz.more_page.profile.ProfileActivity

class MaleFragment : Fragment() {
    private var _binding: FragmentMaleBinding? = null
    private val binding get() = _binding!!
    private var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaleBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUserID = auth.currentUser?.uid.toString()
        reference = FirebaseDatabase.getInstance().reference.child("users").child(currentUserID)

        val sharedPreference: SharedPreferences =
            activity?.applicationContext?.getSharedPreferences(
                "user_status",
                Context.MODE_PRIVATE
            )!!
        val status = sharedPreference.getString("status", null)

        val bundle = this.arguments
        val uid = bundle!!.getString("uid").toString()

        val ref = db.collection("all_anketa")
        ref.document(uid).get()
            .addOnSuccessListener {
                val year = it.data?.get("year").toString() + getString(R.string.year_info)
                val province = it.data?.get("province").toString()
                val nation = it.data?.get("nation").toString()
                val tall = it.data?.get("tall").toString() + " sm"
                val weight = it.data?.get("weight").toString() + " kg"
                val health = it.data?.get("health").toString()
                val marriage = it.data?.get("marriage").toString()
                val prayer = it.data?.get("prayer").toString()
                val profession = it.data?.get("profession").toString()
                val condition = it.data?.get("condition").toString()
                binding.txtYear.text = getString(R.string.yili_info) + year
                binding.txtProvince.text = getString(R.string.yashash) + province
                binding.txtNation.text = getString(R.string.millati) + nation
                binding.txtTall.text = getString(R.string.boyi) + tall
                binding.txtWeight.text = getString(R.string.vazni) + weight
                binding.txtHealth.text = getString(R.string.sogligi) + health
                binding.txtMarriage.text = getString(R.string.nikohi) + marriage
                binding.txtPrayer.text = getString(R.string.ibodatdaligi) + prayer
                binding.txtProfession.text = getString(R.string.kasbi) + profession
                binding.txtCondition.text = getString(R.string.nomzod_sharti) + condition
                binding.txtDate.text = it.data?.get("date").toString()
                binding.txtTime.text = it.data?.get("time").toString()
            }

        binding.chat.setOnClickListener {
            checkNetwork(uid, currentUserID, status)
        }
        return binding.root
    }

    private fun checkNetwork(uid: String, currentUserID: String, status: String?): Boolean {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected || !networkInfo.isAvailable) {
            Toast.makeText(requireContext(), "Intenet yo'q", Toast.LENGTH_SHORT).show()
        }
        statusCheck(uid, currentUserID, status)
        return networkInfo != null && networkInfo.isConnected
    }

    private fun statusCheck(uid: String, currentUserID: String, status: String?) {
        when (status) {
            "no" -> {
                val xabars = getString(R.string.toldir_anketa)
                dialogShow(xabars)
            }
            "wait" -> {
                Toasty.warning(
                    requireContext(), "Anketangiz tasdiqlanmagan kuting...",
                    Toasty.LENGTH_SHORT, true
                ).show()
            }
            "success" -> {
                val intent =
                    Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
            }
            "yes" -> {
                //User oldin ushbu userga yozganmi yoki yo'q tekshirish
                userNullCheck(uid, currentUserID)
            }
        }
    }

    private fun userNullCheck(uid: String, currentUserID: String) {
        val refs = db.collection("user_permission")
        refs.document(currentUserID).get()
            .addOnSuccessListener {
                when (it.data?.get(uid).toString()) {
                    "null" -> {
                        //Userda hozir balansida pul bormi yoki yo'q tekshirish
                        userCheck(uid, currentUserID)
                    }
                    uid -> {
                        Toast.makeText(requireContext(), "Bor", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Boshqa user bor", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun userCheck(uid: String, currentUserID: String) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.child("balance").value.toString()
                Toast.makeText(requireContext(), balance, Toast.LENGTH_SHORT).show()
                if (balance == "0") {
                    val xabar = getString(R.string.sizda_pul_yoq)
                    dialogShow(xabar)
                } else {
                    //Userga yozishga ruxsat berilyabdi
                    val dataMap = hashMapOf(uid to uid)
                    db.collection("user_permission").document(currentUserID).set(dataMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Add", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun dialogShow(xabars: String) {
        CustomizedAlertDialog.callAlertDialog(requireContext(), "Diqqat!",
            xabars,
            "To'ldirish", "Ortga", false,
            object : CustomizedAlertDialogCallback<String> {
                override fun alertDialogCallback(callback: String) {
                    if (callback == "1") {
                        if (xabars == getString(R.string.toldir_anketa)) {
                            val intent =
                                Intent(requireContext(), ProfileActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(requireContext(), "Pul soladi", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
    }
}