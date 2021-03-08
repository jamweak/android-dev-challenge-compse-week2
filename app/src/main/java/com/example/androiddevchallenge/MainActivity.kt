/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            countDownTimer()
        }
    }
}

@Composable
fun countDownTimer(viewModel: InputViewModel = viewModel()) {
    val inputText by viewModel.inputText.observeAsState("")
    val time =
        if (inputText.isEmpty()) 0 else inputText.toInt()
    var animateCount by remember { mutableStateOf(0) }
    var showAnim by remember { mutableStateOf(false) }
    val intToVector: TwoWayConverter<Int, AnimationVector1D> =
        TwoWayConverter({ AnimationVector1D(it.toFloat()) }, { it.value.roundToInt() })
    val countDownAnim = remember {
        Animatable(
            initialValue = 0,
            typeConverter = intToVector
        )
    }

    LaunchedEffect(animateCount) {
        if (animateCount != 0) {
            val result = countDownAnim.animateTo(
                time,
                animationSpec = tween(
                    durationMillis = time,
                    easing = LinearEasing
                )
            )
            if (result.endReason == AnimationEndReason.Finished) {
                showAnim = false
                viewModel.onValueChange("")
                println("${countDownAnim.value}")
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(color = darkColors().background)
                .padding(top = 30.dp)
                .height(400.dp)
                .width(400.dp)
        ) {
            if (showAnim) {
                CircularProgressIndicator(
                    strokeWidth = 8.dp,
                    color = darkColors().primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    progress = (
                        if (time == 0) 0f
                        else (time - countDownAnim.value) / time.toFloat()
                        )
                )
            }

            Text(
                text = timeFormatter(if (showAnim) (time - countDownAnim.value) else time),
                fontSize = 100.sp,
                color = Color.White,
            )
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                if (!showAnim) {
                    viewModel.onValueChange(it)
                }
            },
            label = { Text("Input number to count down") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                if (time > 0) {
                    showAnim = true
                    animateCount++
                }
            },
            content = {
                Text(text = "start")
            }
        )
    }
}

private fun timeFormatter(timeInMillis: Int): String {
    val timeInSec = timeInMillis / 1000
    val min = timeInSec / 60
    val sec = timeInSec % 60
    val minStr = if (min > 9) min else "0$min"
    val secStr = if (sec > 9) sec else "0$sec"
    return "$minStr:$secStr"
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}

class InputViewModel : ViewModel() {
    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> = _inputText

    fun onValueChange(newTime: String) {
        _inputText.value = newTime
    }
}
