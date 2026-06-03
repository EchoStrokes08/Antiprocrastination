package com.example.antiprocrastination.ui.components.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.antiprocrastination.R

fun setLocale(languageCode: String) {
    val locales = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(locales)
}

@Composable
fun LanguageSelector() {
    Column {
        Text(stringResource(R.string.language))
        Button(onClick = { setLocale("en") }) {
            Text("English")
        }
        Button(onClick = { setLocale("es") }) {
            Text("Español")
        }
    }
}