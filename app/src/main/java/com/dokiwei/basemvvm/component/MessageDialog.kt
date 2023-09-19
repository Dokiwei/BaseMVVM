package com.dokiwei.basemvvm.component

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StyleRes
import com.dokiwei.basemvvm.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author DokiWei
 * @date 2023/9/17 23:01
 */
class MessageDialog(context: Context, @StyleRes style: Int, view: View) : Dialog(context, style) {
    private val messageTextView: TextView


    init {
        messageTextView = view.findViewById(R.id.message_messageDialog)
    }

    fun updateMessage(message: String) {
        messageTextView.text = message
    }

    fun addMessage(message: String) {
        val s = "${messageTextView.text}\n${
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))
        }:${message}"
        messageTextView.text = s
    }

    class Build(
        private val context: Context,
        @StyleRes private val style: Int,
        private val view: View
    ) {
        private val titleTextView: TextView = view.findViewById(R.id.title_messageDialog)
        private val messageTextView: TextView = view.findViewById(R.id.message_messageDialog)
        private val cancelButton =
            view.findViewById(R.id.cancel_button_messageDialog) as Button
        private val confirmButton =
            view.findViewById(R.id.confirm_button_messageDialog) as Button
        private var cancelButtonListener: DialogInterface.OnClickListener? = null
        private var confirmButtonListener: DialogInterface.OnClickListener? = null


        fun setTitle(title: String): Build {
            titleTextView.text = title
            return this
        }

        fun setMessage(message: String): Build {
            messageTextView.text = message
            return this
        }

        fun setConfirmButton(text: String, listener: DialogInterface.OnClickListener): Build {
            this.confirmButton.text = text
            this.confirmButtonListener = listener
            return this
        }

        fun setCancelButton(text: String, listener: DialogInterface.OnClickListener): Build {
            this.cancelButton.text = text
            this.cancelButtonListener = listener
            return this
        }

        fun create(): MessageDialog {
            val dialog = MessageDialog(context, style, view)
            dialog.addContentView(
                view, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            cancelButtonListener?.let { clickListener ->
                cancelButton.setOnClickListener {
                    clickListener.onClick(
                        dialog,
                        -1
                    )
                }
            }
            confirmButtonListener?.let { clickListener ->
                confirmButton.setOnClickListener {
                    clickListener.onClick(
                        dialog,
                        -1
                    )
                }
            }
            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            val window = dialog.window
            window?.setWindowAnimations(R.style.dialog_anim)
            return dialog
        }

    }

}