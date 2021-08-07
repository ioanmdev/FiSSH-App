package ro.ioanm.fissh

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.sqlcipher.database.SQLiteDatabase
import ro.ioanm.fissh.core.Selfish
import ro.ioanm.fissh.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init selfish (self signed utils, database, etc ...)
        SQLiteDatabase.loadLibs(this)
        Selfish(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when ((destination as FragmentNavigator.Destination).className) {
                SettingsFragment::class.qualifiedName -> {
                    binding.fab.hide()
                }
                else -> {
                    binding.fab.show()
                }
            }
        }

        binding.fab.setOnClickListener { _ ->
            navController.navigate(R.id.action_MainFragment_to_SettingsFragment)
        }

        showLicenseFirstTime()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_about -> {
                showLicense()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun showLicenseFirstTime() {
        val PREFS_NAME = "FiSSH"
        val settings = getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        if (settings.getBoolean("license_seen", false)) return
        showLicense()
        editor.putBoolean("license_seen", true)
        editor.commit()
    }

    private fun showLicense() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("MIT License")
        builder.setMessage(
            """
            Copyright (c) 2021 Ioan Moldovan
            
            Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:
            
            The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.
            
            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.
            
            """.trimIndent()
        )
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
        val dlg: AlertDialog = builder.create()
        dlg.show()
    }



}