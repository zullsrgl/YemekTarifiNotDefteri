package com.zulalsarioglu.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import kotlin.Exception

class TarifFragment : Fragment() {
    lateinit var button: Button
    lateinit var image: ImageView
    var secilenGorsel: Uri? = null
    var secilenBitmap :Bitmap? = null
    lateinit var isimText :TextView
    lateinit var malzemeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootView =  inflater.inflate(R.layout.fragment_tarif, container, false)
        button = rootView.findViewById(R.id.button)
        image =rootView.findViewById(R.id.imageView)
        isimText =rootView.findViewById(R.id.isimText)
        malzemeText =rootView.findViewById(R.id.melzemeText)


        return rootView


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener{kaydet(it)}
        image.setOnClickListener { gorselSec(it)
        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (gelenBilgi.equals("menudengeldim")){
                isimText.setText("")
                malzemeText.setText("")

                button.visibility = View.VISIBLE

                val gorselsecmearkaplanı = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                image.setImageBitmap(gorselsecmearkaplanı)

            }else{
                button.visibility=View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id
                context?.let {
                     try {
                         val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null )
                         val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                         val yemekIsmiIndex = cursor.getColumnIndex("yemekAdı")
                         val yemekMalzemeIndex = cursor.getColumnIndex("yemekMalsemesi")
                         val yemekGorseli = cursor.getColumnIndex("gorsel")



                         while(cursor.moveToNext()){
                             isimText.setText(cursor.getString(yemekIsmiIndex))
                             malzemeText.setText(cursor.getString(yemekMalzemeIndex))

                             val byteDizisi = cursor.getBlob(yemekGorseli)
                             val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                             image.setImageBitmap(bitmap)
                         }
                         cursor.close() 

                     }catch (e: Exception){
                         e.printStackTrace()
                     }
                }

            }
        }
        }



    }
    fun kaydet(view: View){
        //SQLite a kaydetme işlemi yapılcak
        var yemekAdi = isimText.text.toString()
        var malzemeler = malzemeText.text.toString()

        if(secilenBitmap != null){
            val kucukBitmap = kucukBitMap(secilenBitmap!!, 300)
            val outPutStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.JPEG,50,outPutStream)
            val byteDizi = outPutStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database?.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekAdı VARCHAR, yemekMalsemesi VARCHAR, gorsel BLOB)")
                    val sqlString = "INSERT INTO yemekler(yemekAdı,yemekMalsemesi,gorsel) VALUES (?,?,?)"
                    val statement = database?.compileStatement(sqlString)
                    statement?.bindString(1,yemekAdi)
                    statement?.bindString(2,malzemeler)
                    statement?.bindBlob(3,byteDizi)
                    statement?.execute()
                }


            }catch (e: Exception){
                e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)
        }



    }
    fun gorselSec(view: View){
        // görsel almak için izin isteneceği kısım
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

                //izin verilmedi izin istmeliyiz
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES),1)
            }else{
                //izin verildi galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){

            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izni aldık
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)

            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            secilenGorsel = data.data

            try {

                context?.let {
                    if(secilenGorsel != null) {
                        if( Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            image.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            image.setImageBitmap(secilenBitmap)
                        }

                    }
                }


            } catch (e: Exception){
                e.printStackTrace()
            }


        }

        super.onActivityResult(requestCode, resultCode, data)
        }

    fun kucukBitMap(kullanicininSectigiBitMap: Bitmap, maxBoyut: Int):Bitmap{
        var width = kullanicininSectigiBitMap.width
        var height = kullanicininSectigiBitMap.height
        var bitMapOranı : Double = width.toDouble() / height.toDouble()

        if (bitMapOranı>1){
            width = maxBoyut
            val kisaltilmisHeight = width /bitMapOranı
            height  = kisaltilmisHeight.toInt()

        }else{
            height = maxBoyut
            val kisaltilmisWidth = height * bitMapOranı
            width= kisaltilmisWidth.toInt()

        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitMap,width,height,true)

     }
    }

