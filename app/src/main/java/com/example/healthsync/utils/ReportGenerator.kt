package com.example.healthsync.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.healthsync.data.model.HealthMetric
import java.io.File
import java.io.FileOutputStream

class ReportGenerator(private val context: Context) {

    data class ReportResult(val previewFile: File, val publicPath: String?)

    fun generateWeeklyPDF(userData: List<HealthMetric>): ReportResult? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("HealthSync Weekly Summary", 50f, 50f, paint)

        // Content
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var yPos = 100f
        userData.forEach { metric ->
            if (metric.name == "---") {
                canvas.drawLine(50f, yPos - 10f, 545f, yPos - 10f, paint)
                yPos += 15f
            } else {
                canvas.drawText("${metric.name}: ${metric.value}", 50f, yPos, paint)
                yPos += 25f
            }
            if (yPos > 800f) return@forEach 
        }

        pdfDocument.finishPage(page)

        // 1. Create a preview file in cache for immediate viewing
        // Using cacheDir is reliable for FileProvider
        val previewFile = File(context.cacheDir, "HealthSync_Report_Preview.pdf")
        
        return try {
            FileOutputStream(previewFile).use { pdfDocument.writeTo(it) }
            
            // 2. Export to public Downloads and get the path
            val publicPath = exportToDownloads(previewFile)
            
            ReportResult(previewFile, publicPath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    private fun exportToDownloads(sourceFile: File): String? {
        val fileName = "HealthSync_Report_${System.currentTimeMillis() / 1000}.pdf"
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    "Downloads/$fileName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                sourceFile.copyTo(targetFile, overwrite = true)
                targetFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}