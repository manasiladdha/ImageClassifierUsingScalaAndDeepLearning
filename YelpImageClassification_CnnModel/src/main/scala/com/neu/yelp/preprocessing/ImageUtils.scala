package com.neu.yelp.preprocessing

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.imgscalr.Scalr
import scala.util.matching.Regex

/**
  * Using Scalr API for image processing
  * Created by Pranay on 3/23/2017
  * Modified by Kunal on 3/25/2017
  */
object ImageUtils {

  val patt_get_jpg_name = new Regex("[0-9]")

  // make image square
  def makeSquare(img:BufferedImage) = {
    //println("Squaring the image....")
    val w = img.getWidth
    val h = img.getHeight
    val dim = List(w, h).min

    img match {
      case x if w == h => img
      case x if w > h => Scalr.crop(img, (w-h)/2, 0, dim, dim)
      case x if w < h => Scalr.crop(img, 0, (h-w)/2, dim, dim)
    }
  }

  // resize pixels
  def resizeImg(img:BufferedImage,width: Int, height: Int) = {
    //println("Resizing the image to " + width + "px X " + height +"px....")
    Scalr.resize(img, Scalr.Method.BALANCED, width, height)
  }

  def pixels2gray(red: Int, green:Int, blue: Int): Int = (red + green + blue) / 3

  def image2Vector(img:BufferedImage): Vector[Int] ={
   // println("Pre-processing Image....")
    val w = img.getWidth
    val h = img.getHeight
    for{
      w1 <- (0 until w).toVector
      h1 <- (0 until h).toVector
    }yield {
      //println("Graying image...")
      val col = img.getRGB(w1, h1)
      val red =  (col & 0xff0000) / 65536//2^16
      val green = (col & 0xff00) / 256//2^8
      val blue = (col & 0xff)
      pixels2gray(red,green,blue)
    }
  }

  def img2Map(imageDir:String, image2BizMap:Map[Int,String]): Map[Int,Vector[Int]] = {
    println("Converting Images into Vector Data....")
    // this will load only required images for the biz ids
    val fileList = new File(imageDir).listFiles().filter( f =>{!f.getName.contains("_") && image2BizMap.keySet.contains(patt_get_jpg_name.findAllIn(f.getName).mkString.toInt)}).toList

    fileList.map(file=>patt_get_jpg_name.findAllIn(file.getName).mkString.toInt->{
      image2Vector(resizeImg(makeSquare(ImageIO.read(file)),128,128))
    }).toMap

  }
}
