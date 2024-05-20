package com.cora.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import br.com.cora.design.compose.theme.ArcoTheme
import br.com.cora.design.compose.theme.color.tokens.DefaultColorTokens
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.cora.app.sample.IconsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(DefaultColorTokens.ActivePrimary)
            ArcoTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    IconsScreen()
                }
            }
        }
    }
}