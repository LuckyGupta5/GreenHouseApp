@file:Suppress("NAME_SHADOWING")

package com.ripenapps.greenhouse.utills

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.Settings
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.utills.Constants.DATE_FORMAT
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.random.Random

@Suppress("DEPRECATION")
object CommonUtils {

    fun isKeyboardOpen(rootView: View): Boolean {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.rootView.height
        val keypadHeight = screenHeight - rect.bottom
        return keypadHeight > screenHeight * 0.15 // 0.15 ratio to consider keyboard open
    }

    @SuppressLint("SimpleDateFormat")
    fun sendMessageTime(date: String?): String? {
        if (date.isNullOrEmpty()) return ""

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("hh:mm a")
        outputFormat.timeZone = TimeZone.getDefault()

        return try {
            val dateTimeStamp = inputFormat.parse(date)
            outputFormat.format(dateTimeStamp!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }


    fun extractDigits(input: String): String {
        val regex = Regex("[\\d.]+")
        val matches = regex.find(input)
        return matches?.value ?: ""
    }

    fun showSettingDialog(context: Context, askedPermission: String = "") {
        try {
            MaterialAlertDialogBuilder(
                context, com.google.android.material.R.style.MaterialAlertDialog_Material3
            ).setTitle(context.getString(R.string.image_permission)).setMessage(
                context.getString(
                    R.string.image_permission_is_required_please_allow_image_permission_from_setting,
                    askedPermission
                )
            ).setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, e.message ?: "", Toast.LENGTH_SHORT).show()
        }
    }

    fun showSettingLocationDialog(context: Context, askedPermission: String = "") {
        try {
            MaterialAlertDialogBuilder(
                context, com.google.android.material.R.style.MaterialAlertDialog_Material3
            ).setTitle(context.getString(R.string.location_permission)).setMessage(
                context.getString(
                    R.string.location_permission_is_required_please_allow_location_permission_from_setting,
                    askedPermission
                )
            ).setPositiveButton(context.getString(R.string.settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }.setNegativeButton(context.getString(R.string.cancel_1), null).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, e.message ?: "", Toast.LENGTH_SHORT).show()
        }
    }

    fun compressImage(filePath: String, quality: Int): String {
        val file = File(filePath)
        val fileType = getFileType(file)
        // Load the original image
        val originalBitmap = if (fileType == "webp") {
            decodeWebP(file)
        } else {
            BitmapFactory.decodeFile(filePath)
        }

        // Create a ByteArrayOutputStream to hold the compressed image data
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Compress the image
        val compressed =
            originalBitmap?.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // Check if compression was successful
        if (compressed != null && !compressed) {
            return ""
        }

        val outputFilePath = createTempFile("auc", ".jpg")
        Log.d("TAG", "compressImage: $outputFilePath")
        // Save the compressed image to the output file path
        try {
            val fos = FileOutputStream(outputFilePath)
            fos.write(byteArrayOutputStream.toByteArray())
            fos.flush()
            fos.close()
            Log.d("TAG", "compressImage: ${outputFilePath.path}")
            return outputFilePath.path
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // Helper function to determine the image file type
    private fun getFileType(file: File): String {
        val inputStream: InputStream = file.inputStream()
        val bytes = ByteArray(12)
        inputStream.read(bytes, 0, 12)
        inputStream.close()

        return when {
            bytes.sliceArray(0..2)
                .contentEquals(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> "jpeg"

            bytes.sliceArray(0..7).contentEquals(
                byteArrayOf(
                    0x89.toByte(),
                    0x50.toByte(),
                    0x4E.toByte(),
                    0x47.toByte(),
                    0x0D.toByte(),
                    0x0A.toByte(),
                    0x1A.toByte(),
                    0x0A.toByte()
                )
            ) -> "png"

            bytes.sliceArray(0..3).contentEquals(
                byteArrayOf(
                    0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte()
                )
            ) && bytes.sliceArray(8..11).contentEquals(
                byteArrayOf(
                    0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte()
                )
            ) -> "webp"

            else -> "unknown"
        }
    }

    fun decodeWebP(file: File): Bitmap? {
        return try {
            val options =
                BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
            BitmapFactory.decodeFile(file.path, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

//    fun isPhoneNumberValid(phoneNumber: String): Boolean {
//        // Check if the phone number length is within the range of 6 to 14
//        if (phoneNumber.length !in 7..14) {
//            return false
//        }
//        if (!phoneNumber.all { it.isDigit() }) {
//            return false
//        }
//        return true
//    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        // Check if the phone number length is exactly 9
        if (phoneNumber.length != 9) {
            return false
        }
        // Check if all characters are digits
        if (!phoneNumber.all { it.isDigit() }) {
            return false
        }
        return true
    }

    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun changeStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.statusBarColor = ContextCompat.getColor(activity, color)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun changeStatusBarColor(fragment: Fragment, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragment.requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            fragment.requireActivity().window.statusBarColor =
                ContextCompat.getColor(fragment.requireContext(), color)
        }
    }

    fun hideKeyboard(view: View, activity: Activity) {
        val manager: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun timeLeft(endDate: String, context: Context): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC time zone for consistency
        val currentTime = Calendar.getInstance().time
        val endDateDate = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            null
        }

        endDateDate ?: return null

        val timeDifferenceInMillis = endDateDate.time - currentTime.time

        if (timeDifferenceInMillis <= 0) {
            return context.getString(R.string.expired)
        }

        val days = timeDifferenceInMillis / (1000 * 60 * 60 * 24)
        val hours = (timeDifferenceInMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes =
            ((timeDifferenceInMillis % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60)

        var timeLeftString = ""
        if (days > 0) {
            timeLeftString += "$days D "
        }
        if (hours > 0) {
            timeLeftString += "$hours h "
        }
        timeLeftString += if (minutes > 0) {
            "$minutes min "
        } else {
            context.getString(R.string.few_seconds)
        }

        return "$timeLeftString " + context.getString(R.string.left).trim()
    }

    fun getTimeRemaining(context: Context): String {
        // Get the current time
        val now = LocalDateTime.now()

        // Get the time for 11:59 PM
        val endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.of(23, 59))

        // Calculate the difference in milliseconds
        val timeDifferenceInMillis = ChronoUnit.MILLIS.between(now, endOfDay)

        // Check if time difference is zero or negative
        if (timeDifferenceInMillis <= 0) {
            return context.getString(R.string.expired)
        }

        // Calculate days, hours, and minutes
        val days = timeDifferenceInMillis / (1000 * 60 * 60 * 24)
        val hours = (timeDifferenceInMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes =
            ((timeDifferenceInMillis % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60)

        // Build the time left string
        var timeLeftString = ""
        if (days > 0) {
            timeLeftString += "$days D "
        }
        if (hours > 0) {
            timeLeftString += "$hours h "
        }

        timeLeftString += if (minutes > 0) {
            "$minutes min "
        } else {
            context.getString(R.string.few_seconds)
        }

        return "$timeLeftString " + context.getString(R.string.left).trim()
    }

    fun featuredTime(endDate: String, context: Context): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC time zone for consistency
        val currentTime = Calendar.getInstance().time

        // Parse the end date
        val endDateDate = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            null
        }

        endDateDate ?: return null

        val calendar = Calendar.getInstance()
        calendar.time = endDateDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val endDateNoon = calendar.time

        val timeDifferenceInMillis = endDateNoon.time - currentTime.time

        if (timeDifferenceInMillis <= 0) {
            return context.getString(R.string.expired)
        }


        val days = timeDifferenceInMillis / (1000 * 60 * 60 * 24)
        val hours = (timeDifferenceInMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes =
            ((timeDifferenceInMillis % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60)

        var timeLeftString = ""
        if (days > 0) {
            timeLeftString += "$days D "
        }
        if (hours > 0) {
            timeLeftString += "$hours h "
        }
        timeLeftString += if (minutes > 0) {
            "$minutes m "
        } else {
            context.getString(R.string.few_seconds)
        }

        return "$timeLeftString " + context.getString(R.string.left).trim()
    }

    fun getCurrentDateFormatted(): String {
        // Get the current date
        val currentDate = LocalDate.now()

        // Define the date format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Format the current date
        return currentDate.format(formatter)
    }


    fun isEndDateInRange(endDateString: String, startDateString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        val endDate = LocalDate.parse(endDateString, formatter)
        val startDate = LocalDate.parse(startDateString, formatter)
        val currentDate = LocalDate.now()
        Log.d("TAG", "isEndDateInRange: $startDate $endDate $currentDate")
        val currentDatePlusOne = currentDate.plusDays(1)
        return endDate.isAfter(currentDatePlusOne) && startDate.isBefore(currentDate)
    }

    fun generateRandomNumber(): Int {
        val minNumber = 1000000
        val maxNumber = 9999999
        return Random.nextInt(minNumber, maxNumber + 1)
    }

    //generate random number
    fun hoursLeft(endDate: String): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC time zone for consistency

        val currentTime = Calendar.getInstance().time

        val endDateDate = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            null
        }

        endDateDate ?: return null

        val timeDifferenceInMillis = endDateDate.time - currentTime.time

        if (timeDifferenceInMillis <= 0) {
            return "Delayed"
        }

        val hours = timeDifferenceInMillis / (1000 * 60 * 60)

        return "$hours hours"
    }

    fun isValidPassword(password: String): Boolean {
        Log.d("TAG", "isValidPassword: $password")
        val passwordRegex =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+\$).{8,}\$")
        return passwordRegex.matches(password)
    }

    fun fullDateAndTimeFormat(inputDate: String?): String {
        if (inputDate.isNullOrEmpty()) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(inputDate) ?: return ""

        val outputFormat = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()

        return outputFormat.format(date) // Output: 20 Mar 2024, 12:00 AM
    }

    fun fullDate(inputDate: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(inputDate) ?: return ""

        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()

        return outputFormat.format(date) // Output: 20 Mar 2024, 12:00 AM
    }


    fun capitalizeName(name: String): String {
        return name.split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }
    }

    fun formatDate(inputDate: String?): String {
        if (inputDate.isNullOrEmpty()) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = sdf.parse(inputDate) ?: return ""

        val calendar = Calendar.getInstance()
        calendar.time = date

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(
                Calendar.DAY_OF_YEAR
            ) -> "Today"

            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(
                Calendar.DAY_OF_YEAR
            ) -> "Yesterday"

            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }


    fun getInitials(input: String): String {
        val names = input.split(" ") // Split the input string into individual words
        val initials = StringBuilder() // StringBuilder to store the initials

        // Iterate through the first two words
        for (i in 0 until minOf(names.size, 2)) {
            // Append the first character of each word to the initials StringBuilder
            if (names[i].isNotEmpty()) {
                initials.append(names[i][0].uppercaseChar())
            }
        }

        return initials.toString() // Return the concatenated initials
    }

    @SuppressLint("SetTextI18n")
    fun startTimer(textView: TextView, orderTimer: String?): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC

        val currentTime = Calendar.getInstance().time

        val orderDate = try {
            dateFormat.parse(orderTimer!!)
        } catch (e: Exception) {
            textView.text = "Invalid date"
            return 0
        }

        // Check if parsing was successful
        orderDate ?: return 0

        // Calculate remaining time for countdown
        val remainingTime = orderDate.time - currentTime.time

        Log.d("TAG", "startTimer: $remainingTime")

        // Check if the remaining time is negative (past date)
        if (remainingTime <= 0) {
            textView.text = "00:00 mins"
            return 0
        }

        // Initialize and start the countdown timer
        object : CountDownTimer(remainingTime, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val minutesRemaining = millisUntilFinished / (60 * 1000)
                val secondsRemaining = (millisUntilFinished % (60 * 1000)) / 1000

                // Update TextView with the remaining time formatted as MM:SS
                textView.text = String.format("%02d:%02d mins", minutesRemaining, secondsRemaining)
            }

            override fun onFinish() {
                // Update TextView when timer finishes
                textView.text = "00:00 mins"
            }
        }.start()
        return 1
    }

    fun requestPermissions(
        activity: Activity, PERMISSION_REQUEST_CODE: Int, permission: Array<String?>
    ): Boolean {
        val isAllPermissionGranted = BooleanArray(1)
        Dexter.withContext(activity).withPermissions(*permission)
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) isAllPermissionGranted[0] = true
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(activity, PERMISSION_REQUEST_CODE)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>, token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
//                showSettingDialog(activity)
            }.onSameThread().check()
        return isAllPermissionGranted[0]
    }

    fun showSettingsDialog(activity: Activity, PERMISSION_REQUEST_CODE: Int) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setMessage(activity.getString(R.string.green_house_requires_this_permission))
        dialog.setTitle(activity.resources.getString(R.string.app_name))
        dialog.setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE)
        }
    }

    fun getRealPathFromURI(activity: Activity, uri: Uri?): String {
        var filePath = ""

        val p = Pattern.compile("(\\d+)$")
        val m = p.matcher(uri.toString())
        if (!m.find()) {
            return filePath
        }
        val imgId = m.group()

        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel = MediaStore.Images.Media._ID + "=?"

        val cursor: Cursor = activity.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, arrayOf<String>(imgId), null
        )!!

        val columnIndex = cursor.getColumnIndex(column[0])

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateToAnotherFormat(
        input: String, inputFormat: String, outputFormat: String = "yyyy-MM-dd"
    ): String? {//dd EEE, yyyy
        val inputFormat = SimpleDateFormat(inputFormat)
        val outputFormat = SimpleDateFormat(outputFormat)
        var dateTimeStamp: Date? = null
        try {
            dateTimeStamp = inputFormat.parse(input)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateTimeStamp?.let { outputFormat.format(it) }
    }

    fun getCurrentDateInUTCString(): String {
        // Create a SimpleDateFormat object with UTC time zone
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        // Get the current date
        val currentDate = Date(System.currentTimeMillis())

        // Format the current date to a string
        return sdf.format(currentDate)
    }

    fun convertDateTime(inputDateTime: String): String {
        // Input date format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Set input time zone to UTC

        // Output date format
        val outputFormat = SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault())
        outputFormat.timeZone =
            TimeZone.getDefault() // Set output time zone to default time zone of the device

        return try {
            // Parse input datetime string
            val date = inputFormat.parse(inputDateTime)

            // Format output datetime string
            outputFormat.format(date!!)
        } catch (e: Exception) {
            // Handle parsing/formatting errors
            e.printStackTrace()
            "" // Return empty string or handle error as needed
        }
    }

    fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        var currentDate = startDate
        currentDate = currentDate.plusDays(1)
        val datesInRange = mutableListOf<LocalDate>()

        while (!currentDate.isAfter(endDate)) {
            datesInRange.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return datesInRange
    }

    fun isStartDateStringBeforeCurrentDate(startDateStr: String): Boolean {
        // Define the date format of your start date string
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Parse the start date string into a Date object
        val startDate = dateFormat.parse(startDateStr)

        // Get the current date in UTC
        val calendarCurrent = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        // Check if the start date is before the current date
        return startDate?.before(calendarCurrent.time) ?: false
    }

    fun EditText.passWordModeOnOrOff(showPassword: Boolean, view: ImageView) {
        this.transformationMethod = if (showPassword) {
            view.setImageResource(R.drawable.ic_eye_off)
            PasswordTransformationMethod()
        } else {
            view.setImageResource(R.drawable.ic_eye_on)
            null
        }
        this.setSelection(this.text?.length ?: 0)
    }

    fun timeLeftWithoutSecond(endDate: String, context: Context): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC time zone for consistency

        val currentTime = Calendar.getInstance().time

        val endDateDate = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            null
        }

        endDateDate ?: return null

        val timeDifferenceInMillis = endDateDate.time - currentTime.time

        if (timeDifferenceInMillis <= 0) {
            return context.getString(R.string.expired)
        }

        val days = timeDifferenceInMillis / (1000 * 60 * 60 * 24)
        val hours = (timeDifferenceInMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes =
            ((timeDifferenceInMillis % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60)

        var timeLeftString = ""
        if (days > 0) {
            timeLeftString += "$days D "
        }
        if (hours > 0) {
            timeLeftString += "$hours h "
        }
        timeLeftString += if (minutes > 0) {
            "$minutes m "
        } else {
            context.getString(R.string.few_seconds)
        }
        return "$timeLeftString " + context.getString(R.string.left)
            .trim() // Remove leading/trailing spaces
    }


    fun Fragment.showCustomToast(message: String) {
        activity?.let {
            CustomToast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun convertUTCDate(inputDateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US).apply {
            timeZone = TimeZone.getDefault() // Adjust this if you want a specific time zone
        }

        return try {
            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }//seller joined date conversion function

    fun openMap(context: Context, latitude: Double, longitude: Double) {
        Log.d("TAG", "openMap: $latitude $longitude")
        val gmmIntentUri =
            Uri.parse("geo:0,0?q=$latitude,$longitude") // This format sets the query for the location
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Please install Google Maps", Toast.LENGTH_LONG).show()
        }
    }


    fun Activity.showCustomToast(message: String) {
        CustomToast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun convertToLocalDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        return LocalDate.parse(date, formatter)
    }

    fun checkPermissions(context: Context): Boolean {

        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(activity: Activity): Boolean {
        val locationManager: LocationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun getShortMonthName(fullMonthName: String): String? {
        val month = Month.valueOf(fullMonthName.toUpperCase(Locale.getDefault()))
        return month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun getCategoryId(type: String): String? {
        return when (type.lowercase()) {
            "fruits" -> {
                Constants.FRUITS
            }

            "vegetables" -> {
                Constants.VEGETABLES
            }

            "dates" -> {
                Constants.DATES
            }

            else -> {
                null
            }
        }
    }

}