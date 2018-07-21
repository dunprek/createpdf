package learning.createpdf

import android.Manifest.permission.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat


import android.widget.Toast

import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toFile
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {


    val RequestPermissionCode = 7


    lateinit var tvDetail: TextView


    lateinit var pdfFileName: String
    lateinit var pdfView: PDFView

    private val REQUEST_CODE = 42
    private val TAG = MainActivity::class.java.simpleName
    private val PDF_DIRECTORY = "/printerqoe_pdf_folder/"

    lateinit var filePath: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        pdfView = findViewById(R.id.pdfView)
        // Adding if condition inside button.

        btn_permission.setOnClickListener({

            // If All permission is enabled successfully then this block will execute.
            if (CheckingPermissionIsEnabledOrNot()) {
                Toast.makeText(this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();
            }

            // If, If permission is not enabled then else condition will execute.
            else {

                //Calling method to enable permission.
                RequestMultiplePermission();

            }
        })

        btn_file.setOnClickListener({
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            try {
                startActivityForResult(intent, REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                //alert user that file manager not working
                Toast.makeText(this, "Error picking file", Toast.LENGTH_SHORT).show()
            }


        })


        tvDetail = findViewById(R.id.tv_detail);


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data == null) {
//                showToast(applicationContext, "gagal mengambil gambar")
                return
            }
            filePath = data.data


            pdfFileName = getFileName(filePath)





            Log.e(TAG, "PATH FILE " + RealPathUtil.getRealPathFromURI_API19(applicationContext,filePath))


            createFolder()
//            displayFromUri(filePath)

        } else {
//            showToast(applicationContext, "gagal mengambil gambar")
        }
    }


    private fun getFileName(uri: Uri?): String {
        var result: String? = null
        if (uri != null) {
            if (uri.scheme == "content") {
                val cursor = contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        Log.d(TAG, "FILE NAME " + result)
                    }
                } finally {
                    cursor?.close()
                }
            }
        }
        if (result == null) {
            if (uri != null) {
                result = uri.lastPathSegment
            }
        }
        return result!!
    }





    private fun RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), RequestPermissionCode)

    }


    // Calling override method.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            RequestPermissionCode ->

                if (grantResults.size > 0) {

                    val CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (CameraPermission && RecordAudioPermission) {

                        Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_LONG).show()

                    }
                }
        }
    }

    fun CheckingPermissionIsEnabledOrNot(): Boolean {

        val FirstPermissionResult = ContextCompat.checkSelfPermission(applicationContext, CAMERA)
        val SecondPermissionResult = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {
        Log.e(TAG, pdfView.pageCount.toString())
    }

    override fun onPageError(page: Int, t: Throwable?) {
    }


    fun createFolder() {
        val pdfDirectory = File(
                Environment.getExternalStorageDirectory().toString() + PDF_DIRECTORY)
        // have the object build the directory structure, if needed.
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs()
        }

        try {
            /*  val f = File(pdfDirectory, pdfFileName)
              f.createNewFile()*/
            /*  val fo = FileOutputStream(f)
              fo.write(pdfFileName.toByteArray())
              fo.close()
              Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

  */



            val sourceFile =File(RealPathUtil.getRealPathFromURI_API19(applicationContext,filePath))
            val destinationFile = File(RealPathUtil.getRealPathFromURI_API19(applicationContext,filePath))

            Log.d(TAG,"MYSOURCE "+sourceFile)
            Log.d(TAG,"MY DESTINATION "+pdfDirectory)

            copy(sourceFile,destinationFile)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun copy(src: File, dst: File) {
        val instream = FileInputStream(src)
        val outStream = FileOutputStream(dst)
        val inChannel = instream.getChannel()
        val outChannel = instream.getChannel()
        inChannel.transferTo(0, inChannel.size(), outChannel)
        instream.close()
        outStream.close()
    }


}
