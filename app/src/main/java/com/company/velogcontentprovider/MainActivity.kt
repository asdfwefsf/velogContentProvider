package com.company.velogcontentprovider

import android.Manifest
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.company.velogcontentprovider.ui.theme.VelogContentProviderTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ImageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                0
            )
        }
        val projection = arrayOf(
            // 이미지 파일에 할당된 ID
            MediaStore.Images.Media._ID,
            // 이미지 파일의 이름
            MediaStore.Images.Media.DISPLAY_NAME,

        )

        val millisYesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -260)
        }.timeInMillis

        // ?가 설정되어 있으므로 selectionArgs의 값으로 대체한다.
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
        // selection에 들어갈 값이다.
        val selectionArgs = arrayOf(millisYesterday.toString())
        // 정렬 순서를 오름차순으로 설정
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)

            val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

            val images = mutableListOf<Image>()
            while(cursor.moveToNext()) {
                // 조회된 컬럼 인덱스를 사용하여 현재 행에서 이미지의 ID와 이름을 추출한다.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)

                // 추출한 이미지 ID를 사용하여 해당 이미지의 완전한 URI를 생성한 후 이 URI를 이미지에 접근할 때 사용한다.
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(Image(id , name , uri))
            }
            viewModel.updateImages(images)
        }
        setContent {
            VelogContentProviderTheme {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.images) { image ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AsyncImage(
                                model = image.uri,
                                contentDescription = null
                            )
                            Text(text = image.name)
                        }
                    }
                }
            }
        }
    }
}
data class Image(
    val id : Long,
    val name : String,
    val uri : Uri
)
