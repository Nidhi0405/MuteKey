package com.bbm.applock.util

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true, showSystemUi = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true, showSystemUi = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true, showSystemUi = true)
@Preview(
    name = "Pixel 4",
    device = "spec:width=411dp,height=891dp,dpi=420",
    showBackground = true,
    showSystemUi = true
)
annotation class MultiDevicePreview