package com.bbm.applock.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import com.bbm.applock.R
import com.bbm.applock.hiltmodule.ComponentActivityInjectModule
import com.bbm.applock.service.AppBlockAccessibilityService
import com.bbm.applock.service.AppBlockAccessibilityService.Companion.finishEvent
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.ui.theme.WhiteColor
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch


class BlockScreenActivity : ComponentActivity() {

    companion object {
        private const val PACKAGE_NAME = "packageName"
        fun instance(context: Context, packageName: String) =
            Intent(context, BlockScreenActivity::class.java).apply {
                putExtra(PACKAGE_NAME, packageName)
            }
    }

    private val imageLoader: ImageLoader by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext, ComponentActivityInjectModule::class.java
        ).imageLoader()
    }

    private val blockedPackageName by lazy { intent.getStringExtra(PACKAGE_NAME) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            finishEvent.collect {
                finishAffinity()
                overridePendingTransition(0, 0)
            }
        }

        setContent {
            AppLockTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = .8f))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WhiteColor)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                /*Image(
                                    painter = rememberAsyncImagePainter(
                                        AppIcon(blockedPackageName!!),
                                        imageLoader = imageLoader
                                    ),
                                    contentScale = ContentScale.Fit,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(start = 40.dp)
                                        .size(80.dp)
                                        .clip(CircleShape)
                                )*/
                                Image(
                                    painter = painterResource(R.drawable.img_thumbs_up),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        // .padding(end = 40.dp)
                                        .size(120.dp)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "You’re doing great!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    brush = TextPrimaryGradient,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.W700
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Hang in there — your restricted time will end shortly. Stay committed to your routine; success is built one disciplined hour at a time.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W400
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 26.dp),
                                onClick = {
                                    onBackPressed()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AquaBlue)
                            ) {
                                Text(
                                    text = stringResource(R.string.ok),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = WhiteColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W600
                                    )
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AppBlockAccessibilityService.instance?.removeAppFromScreen()
        finishAffinity()
    }
}