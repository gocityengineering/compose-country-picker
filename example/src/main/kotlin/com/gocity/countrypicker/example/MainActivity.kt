package com.gocity.countrypicker.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gocity.countrypicker.example.ui.theme.EmojiCountryPickerTheme
import com.gocity.countrypicker.model.Country
import com.gocity.countrypicker.model.PhoneNumberValidity
import com.gocity.countrypicker.ui.CountryPicker
import com.gocity.countrypicker.ui.PhoneNumberPicker
import com.gocity.countrypicker.ui.PhoneNumberPickerDefaults
import com.gocity.countrypicker.ui.rememberPhoneNumberState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EmojiCountryPickerTheme {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .fillMaxSize()

                ) { padding ->
                    Column(
                        Modifier
                            .padding(padding)
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        var country by rememberSaveable { mutableStateOf<Country?>(null) }
                        CountryPicker(currentCountry = country, shape = CircleShape) {
                            country = it
                        }
                        Spacer(Modifier.height(24.dp))
                        val phoneNumber = rememberPhoneNumberState()
                        PhoneNumberPicker(
                            phoneNumber,
                            shape = CircleShape,
                            supportingText = { validity, isError ->
                                when {
                                    isError -> Text(PhoneNumberPickerDefaults.errorMessage(validity))
                                    validity == PhoneNumberValidity.Valid -> Text(
                                        phoneNumber.e164.orEmpty(),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
