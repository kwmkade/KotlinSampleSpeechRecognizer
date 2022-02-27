package com.kwmkade.kotlinsamplespeechrecognizer

import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity(), View.OnClickListener {

    // 参照
    private lateinit var label: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportActionBar?.hide()

        // 参照
        this.label = findViewById(R.id.label)
        this.label.text = ""
        this.button = findViewById(R.id.button)
        this.button.setOnClickListener(this) // NOTE: MainActivityに View.OnClickListener を継承させているので
        setUiActive(false)

        // 音声認識の開始
        // NOTE: PermissionsDispatcherにより @NeedsPermission で宣言した関数に「WithPermissionCheck」付加された関数が自動生成される
        setupRecognizerWithPermissionCheck()
    }

    //====================
    // 操作
    //====================
    // UIの有効化・無効化
    private fun setUiActive(active: Boolean) {
        runOnUiThread {
            this.button.isEnabled = active
        }
    }

    // テキストの指定
    private fun setText(text: String) {
        runOnUiThread {
            this.label.text = text
        }
    }

    // クリック時に呼ばれる
    // NOTE: MainActivityに View.OnClickListener を継承させているので
    override fun onClick(v: View) {
        // 音声認識の開始
        startRecognizer()
    }

    //====================
    // パーミッション
    //====================

    // 許可された時に呼ばれる
    @NeedsPermission(RECORD_AUDIO)
    fun setupRecognizer() {
        setUiActive(true)
    }

    // 説明が必要な時に呼ばれる
    @OnShowRationale(RECORD_AUDIO)
    fun onCameraShowRationale(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton("許可") { _, _ -> request.proceed() }
            .setNegativeButton("許可しない") { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage("マイクを利用します")
            .show()
    }

    // 拒否された時に呼ばれる
    @OnPermissionDenied(RECORD_AUDIO)
    fun onCameraPermissionDenied() {
        Toast.makeText(this, "拒否されました", Toast.LENGTH_SHORT).show()
    }

    // 「今後表示しない」が選択された時に呼ばれる
    @OnNeverAskAgain(RECORD_AUDIO)
    fun onCameraNeverAskAgain() {
        Toast.makeText(this, "「今後表示しない」が選択されました", Toast.LENGTH_SHORT).show()
    }

    //====================
    // 音声認識
    //====================
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == Activity.RESULT_OK) {
                result.data?.let { data: Intent ->
                    val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0]
                    setText(text)
                }
            }
        }

    private fun startRecognizer() {
        // RecognizerIntentの生成
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // アクティビティの起動
        activityLauncher.launch(intent)
    }

}