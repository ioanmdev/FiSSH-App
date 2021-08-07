package ro.ioanm.fissh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ro.ioanm.fissh.core.Computer
import ro.ioanm.fissh.core.Selfish
import ro.ioanm.fissh.databinding.FragmentSettingsBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null) {
            binding.txtComputerIp.setText(requireArguments().getString("computer_ip"))
            binding.txtNickname.setText(requireArguments().getString("nickname"))
            binding.txtPassword.setText(requireArguments().getString("password"))
        }

        addComputerIPErrorCheck()

        binding.btnSave.setOnClickListener {
            validateComputerIP()
            if (binding.txtComputerIp.error == null) {
                if (arguments == null)
                {
                    Selfish.selfish.DB.addComputer(Computer(binding.txtNickname.text.toString(), binding.txtComputerIp.text.toString(), binding.txtPassword.text.toString()))
                }
                else
                {
                    val pos = requireArguments().getInt("id")
                    val toEdit = Selfish.selfish.DB.computers[pos]
                    toEdit.Nickname = binding.txtNickname.text.toString()
                    toEdit.ComputerIP = binding.txtComputerIp.text.toString()
                    toEdit.Password = binding.txtPassword.text.toString()
                    Selfish.selfish.DB.updateComputer(toEdit)
                }

                findNavController().navigateUp()
            }
        }
    }

    private fun addComputerIPErrorCheck() {
        binding.txtComputerIp.onFocusChangeListener = OnFocusChangeListener { _, b -> // Check on defocus
            if (!b) validateComputerIP()
        }
    }

    private fun validateComputerIP() {
        if (binding.txtComputerIp.text.toString() == "") {
            // Check if there's already an error set
            if (binding.txtComputerIp.error != null) return
            binding.txtComputerIp.error = "Please enter Computer IP"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}