package com.example.twitter

class Ticket{
    var tweetID:String?=null
    var tweetText:String?=null
    var tweetImageUrl:String?=null
    var tweetPersonUID:String?=null

    constructor(tweetID:String,tweetText:String,tweetImageUrl:String,tweetPersonUID:String){
        this.tweetID=tweetID
        this.tweetText=tweetText
        this.tweetImageUrl=tweetImageUrl
        this.tweetPersonUID=tweetPersonUID


    }
}