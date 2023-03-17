package uz.ibrohim.sovchiuz

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.ibrohim.sovchiuz.databinding.ActivityHomeBinding
import uz.ibrohim.sovchiuz.language.AppCompat

class HomeActivity : AppCompat() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        navController = findNavController(R.id.home_fragment)
        setupSmoothBottomMenu()

        val currentUserID = auth.currentUser?.uid
        reference.child("users").child(currentUserID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("user_status", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    val name: String? = sharedPreferences.getString("name", null)
                    val balance = snapshot.child("balance").value.toString()
                    when (snapshot.child("status").value.toString()) {
                        "no" -> {
                            editor.apply {
                                putString("status", "no")
                                putString("name", name)
                                putString("balance", balance)
                            }.apply()
                        }
                        "wait" -> {
                            editor.apply {
                                putString("status", "wait")
                                putString("name", name)
                                putString("balance", balance)
                            }.apply()
                        }
                        "success" -> {
                            editor.apply {
                                putString("status", "success")
                                putString("name", name)
                                putString("balance", balance)
                            }.apply()
                        }
                        "block" -> {
                            editor.apply {
                                putString("status", "block")
                                putString("name", name)
                                putString("balance", balance)
                            }.apply()
                        }
                        "yes" -> {
                            editor.apply {
                                putString("status", "yes")
                                putString("name", name)
                                putString("balance", balance)
                            }.apply()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_home)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

//        val sharedPreference: SharedPreferences =
//            this.getSharedPreferences("user_status", Context.MODE_PRIVATE)
//        val login: String? = sharedPreference.getString("status", null)
//        val name: String? = sharedPreference.getString("name", null)
//        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()