package minorproject.two.leafblightdetection

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class Categorization(assetManager: AssetManager, modelPath: String, labelPath: String, inputSize: Int) {
    private val GVN_INP_SZ: Int = inputSize
    private val PHOTO_SDEVIATE = 255.0f
    private val GREAT_OUTCOME_MXX = 3
    private var PITNR: Interpreter
    private var ROW_LINE: List<String>
    private val IMAGE_PXL_SZ: Int = 3
    private val PHOTO_MEN = 0
    private val POINT_THRHLDD = 0.4f

    /*


    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvPosition
            .getLayoutParams();
        int start = (int) (MARGIN
            + (leftProgress/*mVideoView.getCurrentPosition()*/ - scrollPos) * averagePxMs);
        int end = (int) (MARGIN + (rightProgress - scrollPos) * averagePxMs);
        animator = ValueAnimator
            .ofInt(start, end)
            .setDuration(
                (rightProgress - scrollPos) - (leftProgress/*mVideoView.getCurrentPosition()*/
                    - scrollPos));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.leftMargin = (int) animation.getAnimatedValue();
                mIvPosition.setLayoutParams(params);



     */


    data class Categorizationn(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F
    )  {
        override fun toString(): String {
            return "Title = $title, Confidence = $confidence)"
        }
    }

    init {
        PITNR = Interpreter(loadModelFile(assetManager, modelPath))
        ROW_LINE = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }

    }

    fun recognizeImage(bitmap: Bitmap): List<Categorization.Categorizationn> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, GVN_INP_SZ, GVN_INP_SZ, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(1) { FloatArray(ROW_LINE.size) }
        PITNR.run(byteBuffer, result)
        return getSortedResult(result)
    }



    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * GVN_INP_SZ * GVN_INP_SZ * IMAGE_PXL_SZ)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(GVN_INP_SZ * GVN_INP_SZ)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until GVN_INP_SZ) {
            for (j in 0 until GVN_INP_SZ) {
                val `val` = intValues[pixel++]

                byteBuffer.putFloat((((`val`.shr(16)  and 0xFF) - PHOTO_MEN) / PHOTO_SDEVIATE))
                byteBuffer.putFloat((((`val`.shr(8) and 0xFF) - PHOTO_MEN) / PHOTO_SDEVIATE))
                byteBuffer.putFloat((((`val` and 0xFF) - PHOTO_MEN) / PHOTO_SDEVIATE))
            }
        }
        return byteBuffer
    }


    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Categorization.Categorizationn> {
        Log.d("Classifier", "List Size:(%d, %d, %d)".format(labelProbArray.size,labelProbArray[0].size,ROW_LINE.size))

        val pq = PriorityQueue(
            GREAT_OUTCOME_MXX,
            Comparator<Categorization.Categorizationn> {
                    (_, _, confidence1), (_, _, confidence2)
                -> java.lang.Float.compare(confidence1, confidence2) * -1
            })

        for (i in ROW_LINE.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= POINT_THRHLDD) {
                pq.add(Categorization.Categorizationn("" + i,
                    if (ROW_LINE.size > i) ROW_LINE[i] else "Unknown", confidence)
                )
            }
        }
        Log.d("Classifier", "pqsize:(%d)".format(pq.size))

        val recognitions = ArrayList<Categorization.Categorizationn>()
        val recognitionsSize = Math.min(pq.size, GREAT_OUTCOME_MXX)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }

}