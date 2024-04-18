### 1 创建
```kotlin
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class StartGameDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Network error!")
                .setPositiveButton("start",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
```

```kotlin
    val sgDiaLog = StartGameDialogFragment()
    sgDiaLog.show(supportFragmentManager, "sg_dia_log")
```


### 2 使用
```kotlin
val builder: AlertDialog.Builder? = activity?.let {
    AlertDialog.Builder(it)
}

// 设置信息和标题
builder?.setMessage(R.string.dialog_message)
        .setTitle(R.string.dialog_title)

// 创建
val dialog: AlertDialog? = builder?.create()
dialog.show()
```
