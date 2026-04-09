package com.example.healthsync.ui.profile

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.healthsync.databinding.FragmentReportViewerBinding
import java.io.File

class ReportViewerFragment : Fragment() {

    private var _binding: FragmentReportViewerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Toolbar as ActionBar to handle back navigation properly
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Weekly Health Report"
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val filePath = arguments?.getString("filePath")
        if (filePath != null) {
            displayPdf(filePath)
        } else {
            Toast.makeText(requireContext(), "Error: No report path provided", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun displayPdf(path: String) {
        try {
            val file = File(path)
            if (!file.exists()) {
                binding.tvReportInfo.text = "Report file not found."
                return
            }

            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            
            if (renderer.pageCount > 0) {
                // Render the first page
                val page = renderer.openPage(0)
                
                // Create a bitmap with the page's dimensions
                val density = resources.displayMetrics.densityDpi
                val bitmap = Bitmap.createBitmap(
                    page.width * (density / 72), 
                    page.height * (density / 72), 
                    Bitmap.Config.ARGB_8888
                )
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                binding.ivReportPage.setImageBitmap(bitmap)
                
                binding.tvReportInfo.text = "Generated on: ${java.util.Date(file.lastModified())}\nPage 1 of ${renderer.pageCount}"
                
                page.close()
            }
            
            renderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvReportInfo.text = "Error rendering PDF: ${e.message}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset the action bar when leaving this fragment
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }
}