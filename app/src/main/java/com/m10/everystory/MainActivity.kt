package com.m10.everystory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EveryStoryTheme {
                // Placeholder until the nav host lands.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(EsSpace.s12, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo_mark),
                            contentDescription = null,
                            modifier = Modifier.size(EsSize.logoMark),
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EsTheme.colors.muted,
                        )
                    }
                }
            }
        }
    }
}
