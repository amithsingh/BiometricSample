package com.example.biomatricauthdemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.biomatricauthdemo.BioMetricPromptManager.BioMetricResult
import com.example.biomatricauthdemo.ui.theme.BioMatricAuthDemoTheme

class MainActivity : AppCompatActivity() {
    private val promptManager by lazy { BioMetricPromptManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioMatricAuthDemoTheme {
                val bioMetricResult by promptManager.promptResult.collectAsState(
                    initial = null
                )

                val enrollLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            if (it.resultCode == RESULT_OK) {
                                promptManager.showBioMetricPrompt(
                                    "Biometric Authentication", "Authenticate using your fingerprint"
                                )
                            }
                        })

                LaunchedEffect(bioMetricResult) {
                    if (bioMetricResult is BioMetricResult.AuthNotSet) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        promptManager.showBioMetricPrompt(
                            "Biometric Authentication", "Authenticate using your fingerprint"
                        )
                    }) {
                        Text(text = "Authenticate")
                    }

                    bioMetricResult?.let {
                        Text(
                            text = when (it) {
                                BioMetricResult.AuthFailed -> "Auth Failed"
                                is BioMetricResult.AuthError -> "Auth Error: ${it.error}"
                                BioMetricResult.AuthNotSet -> "Auth Not Set"
                                BioMetricResult.AuthSuccess -> "Auth Success"
                                BioMetricResult.FeatureUnavailable -> "Auth Feature Unavailable"
                                BioMetricResult.HardwareUnavailable -> "Auth Hardware Unavailable"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BioMatricAuthDemoTheme {
        Greeting("Android")
    }
}