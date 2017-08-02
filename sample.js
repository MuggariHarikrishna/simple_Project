var express = require('express');
var fs      = require('fs');
var request = require('request');
var cheerio = require('cheerio');
var app     = express();

var i=1;
for(i=1;i<=69;i++){
  url = 'https://www.behindthename.com/names/'+i;

  request(url, function(error, response, html){
    if(!error){
      var $ = cheerio.load(html);

        console.log("process started");
	$('.browsename').each(function(){
       console.log($(this).children().first().children().first().text().trim()+"\t"+$(this).children(":nth-child(2)").text().trim());
       //console.log($(this).children(":nth-child(2)").text().trim());
   });
    }
    }); 
}
