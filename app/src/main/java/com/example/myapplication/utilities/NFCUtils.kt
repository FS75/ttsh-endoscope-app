package com.example.myapplication.utilities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.R

class NFCUtils {

    companion object {
        const val TAG = "NFCUtils"

        //NFC stuff
        private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
        private var intentFiltersArray: Array<IntentFilter>? = null
        private val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        private var pendingIntent: PendingIntent? = null
        private var nfcAdapter: NfcAdapter? = null
        private var nfcCardTag: String? = null

        //Use to set up NFC adapter for first time
        fun setNFCAdapter(adapter: NfcAdapter?, activity: AppCompatActivity) {
            nfcAdapter = adapter
            if (nfcAdapter == null) {
                //Device don't support NFC
            } else if (!nfcAdapter!!.isEnabled) {
                //Device NFC not enabled, set up builder for popup message
                val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
                builder.setTitle("NFC Disabled")
                builder.setMessage("Please Enable NFC")

                builder.setPositiveButton("Settings") { _, _ ->
                    startActivity(
                        activity, Intent(Settings.ACTION_NFC_SETTINGS), null
                    )
                }
                builder.setNegativeButton(R.string.cancel, null)
                val myDialog = builder.create()
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()
            }
        }

        //To set up NFC
        fun onNFCStart(activity: AppCompatActivity, javaClass: Class<AppCompatActivity>) {
            pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            try {
                ndef.addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
            intentFiltersArray = arrayOf(ndef)
        }

        //NFC will call this intent
        fun onNFCDetected(intent: Intent, context: Context): Array<out NdefRecord>? {
            context?.let { playNotificationSound(it) }
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {
                try {
                    val inNdefMessage = this?.get(0) as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    if (inNdefMessage.records.size > 0) {
                        var ndefRecord_0 = inNdefRecords[0]
                        nfcCardTag = String(ndefRecord_0.payload).drop(3)
                        return inNdefRecords
                    } else {
                        android.widget.Toast.makeText(
                            context, R.string.NFC_no_records, android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (ex: Exception) {
                    android.widget.Toast.makeText(
                        context, ex.toString(), android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return null
            }
        }

        //To be called in onPause
        fun onNFCPause(activity: AppCompatActivity) {
            if (activity.isFinishing) {
                nfcAdapter?.disableForegroundDispatch(activity)
            }
        }

        //To be called in onResume
        fun onNFCResume(activity: AppCompatActivity) {
            nfcAdapter?.enableForegroundDispatch(
                activity, pendingIntent, intentFiltersArray, techListsArray
            )
        }

        //To get the card tag - staff, assistant or scope
        fun getCardTag(): String? {
            return nfcCardTag
        }

        //Sound
        fun playNotificationSound(context: Context) {
            val mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
        }
    }
}