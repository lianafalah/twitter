package com.example.twitter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference
    var ListTweets=ArrayList<Ticket>()
    var adpater:MyTweetAdpater?=null
    var myEmail:String?=null
    var UserUID:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var b:Bundle=intent.extras
        myEmail=b.getString("email")
        UserUID=b.getString("uid")

        ListTweets.add(Ticket("0","him","url","add"))
        ListTweets.add(Ticket("0","him","url","add"))
        ListTweets.add(Ticket("0","him","url","add"))
        ListTweets.add(Ticket("0","him","url","add"))
        adpater=MyTweetAdpater(this,ListTweets)
        lvTweets.adapter=adpater

    }
    inner class MyTweetAdpater: BaseAdapter {
        var listNotesAdpater = ArrayList<Ticket>()
        var context:Context?=null
        constructor(context: Context, ListTweetAdpater: ArrayList<Ticket>) : super() {
            this.listNotesAdpater = ListTweetAdpater
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var mytweets = listNotesAdpater[position]
            if (mytweets.tweetPersonUID.equals("add")) {
                var myView = layoutInflater.inflate(R.layout.add_ticket,null)
                //load add ticket
                //TODO: work
                myView.iv_attach.setOnClickListener(View.OnClickListener {
                    loadImage()
                })
                myView.iv_post.setOnClickListener(View.OnClickListener {
                    myRef.child("posts").push().setValue(
                        PostInfo(UserUID!!,
                        myView.etPost.text.toString(),DownloadURL!!))
                        myView.etPost.setText("")
                })
                return myView
            } else {
                var myView = layoutInflater.inflate(R.layout.tweets_ticket, null)
                //load tweet ticket
                return myView
            }
        }
        override fun getItem(position: Int): Any {
            return listNotesAdpater[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listNotesAdpater.size
        }
    }
    val PICK_IMAGE_CODE=123
    fun loadImage(){
        //load image
        var intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_CODE && data != null){
            val selectedImage=data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor= contentResolver.query(selectedImage,filePathColum,null,null, null )
            cursor.moveToFirst()
            val coulomIndex=cursor.getColumnIndex(filePathColum[0])
            val picturePath=cursor.getString(coulomIndex)
            cursor.close()
            UploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }

    var DownloadURL:String?=""
    fun UploadImage(bitmap:Bitmap){

        val storage= FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitter-979ff.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath=SplitString(myEmail!!) +"." +df.format(dataobj)+ ".jpg"
        val ImageRef=storageRef.child("imagePost/"+imagePath)
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data =baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->

           DownloadURL=taskSnapshot.getStorage().getDownloadUrl().toString()

        }
    }
    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }
    fun LoadPost(){
        myRef.child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                try {
                    var td= dataSnapshot!!.value as HashMap<String,Any>
                    for (key in td.keys){
                        var post= td[key] as HashMap<String,Any>
                        ListTweets.add(Ticket(key,
                            post["text"] as String,
                            post["postImage"] as String
                            ,post["userUID"] as String))
                    }
                    adpater!!.notifyDataSetChanged()
                }catch (ex:Exception){}
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }


}
