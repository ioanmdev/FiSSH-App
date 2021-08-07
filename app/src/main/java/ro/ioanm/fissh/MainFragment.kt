package ro.ioanm.fissh

import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import ro.ioanm.fissh.core.Computer
import ro.ioanm.fissh.core.Selfish
import ro.ioanm.fissh.core.TCPMessenger
import ro.ioanm.fissh.databinding.FragmentMainBinding
import java.util.concurrent.Executor


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var COMPUTERS: ArrayList<Computer>
    private lateinit var ADAPTER: ArrayAdapter<Computer>

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private  var toSend: Computer? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        COMPUTERS = ArrayList<Computer>()
        // Prepare to load computers from Database
        ADAPTER = object : ArrayAdapter<Computer>(requireActivity(), android.R.layout.simple_list_item_2, android.R.id.text1) {
            override fun getView(
                position: Int,
                @Nullable convertView: View?,
                parent: ViewGroup
            ): View {
                val theView = super.getView(position, convertView, parent)
                val text1 = theView.findViewById<View>(android.R.id.text1) as TextView
                val text2 = theView.findViewById<View>(android.R.id.text2) as TextView
                text1.text = COMPUTERS[position].Nickname
                text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                text1.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.lvPadding))
                text2.text = COMPUTERS[position].ComputerIP
                text2.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.lvPadding))
                return theView
            }
        }

        binding.lvComputers.adapter = ADAPTER



        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(),
                        "Fingerprint Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val msg = TCPMessenger(this@MainFragment, toSend)
                    msg.run()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Fingerprint Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication for FiSSH")
                .setSubtitle("Confirm your identity using your Fingerprint")
                .setDeviceCredentialAllowed(false)
                .setNegativeButtonText("Cancel")
                .build()

        binding.lvComputers.onItemClickListener = OnItemClickListener { _, _, i, _ ->
            toSend = COMPUTERS[i]
            biometricPrompt.authenticate(promptInfo)
        }

        registerForContextMenu(binding.lvComputers)
        loadComputers()
    }

    private fun loadComputers() {
        COMPUTERS = Selfish.selfish.DB.computers
        ADAPTER.clear()
        ADAPTER.addAll(COMPUTERS)
        ADAPTER.notifyDataSetChanged()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (v.id == R.id.lvComputers) {
            menu.setHeaderTitle("What do you want to do?")
            menu.add(Menu.NONE, 0, 0, "Edit computer")
            menu.add(Menu.NONE, 1, 1, "Delete computer")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        when (item.itemId) {
            0 -> editComputer(info.position)
            1 -> removeComputer(info.position)
        }
        return true
    }

    private fun editComputer(pos: Int) {
        val bundle = Bundle()

        bundle.putString("nickname", COMPUTERS[pos].Nickname)
        bundle.putString("computer_ip", COMPUTERS[pos].ComputerIP)
        bundle.putString("password", COMPUTERS[pos].Password)
        bundle.putInt("id", pos)

        findNavController().navigate(R.id.action_MainFragment_to_SettingsFragment, bundle)
    }

    private fun removeComputer(pos: Int) {
        // Display an error message
        // Display an error message
        val bld: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        bld.setTitle("Delete computer?")

        bld.setMessage("Are you sure you want to remove this computer from FiSSH?")

        bld.setNegativeButton("No",
            DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() })

        bld.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
                Selfish.selfish.DB.deleteComputer(COMPUTERS[pos])
                loadComputers()
            })

        val errorMsg: AlertDialog = bld.create()
        errorMsg.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun reportNetworkError()
    {
        // Display an error message
        val bld = AlertDialog.Builder(requireContext())

        bld.setTitle("Error")
        bld.setMessage("Network Error! Check your connection and Try again!")

        bld.setPositiveButton(
            "OK"
        ) { dialogInterface, _ -> dialogInterface.dismiss() }

        val errorMsg = bld.create()
        errorMsg.show()
    }

    fun reportUnknownCertificate( oldFingerprint : String, newFingerprint : String, toSave : ByteArray)
    {
        val bld = AlertDialog.Builder(requireContext())

        bld.setTitle("Connection Aborted")

        if (oldFingerprint.equals("NONE")) bld.setMessage(
            """
                FiSSH now supports self-signed certificate validation to prevent Man-In-The-Middle attacks
                
                Please confirm that your certificate's fingerprint is:
                ${newFingerprint.toString()}
                
                If you confirm this certificate, it will be stored and trusted from now on.
                """.trimIndent()
        ) else bld.setMessage(
            """
                Unknown Certificate!
                
                Warning: This could be a Man-In-The-Middle attack
                
                The fingerprint of the NEW certificate is:
                ${newFingerprint.toString()}
                
                And the fingerprint of the trusted (stored) certificate is:
                ${oldFingerprint.toString()}
                
                Should I trust the new one from now on?
                """.trimIndent()
        )

        bld.setNegativeButton(
            "No"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "New certificate REJECTED by user!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        bld.setPositiveButton(
            "Yes"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            try {
                toSend!!.Certificate = toSave
                Selfish.selfish.DB.updateComputer(toSend)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "New certificate APPROVED by user!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        val errorMsg = bld.create()
        errorMsg.show()
    }

    fun authorizationSent() {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Authorization sent to " + toSend!!.Nickname + " (" + toSend!!.ComputerIP + ").",
            Snackbar.LENGTH_LONG
        ).show()
        toSend = null
    }

}