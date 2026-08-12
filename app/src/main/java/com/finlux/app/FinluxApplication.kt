package com.finlux.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application-level dependency container for Finlux. */
@HiltAndroidApp
class FinluxApplication : Application()
