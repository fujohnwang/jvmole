package me.afoo

case class Developer(name: String, mail: String, blog: String, twitter: String, weibo: String, remark: String = "")

trait ProjectInfo {
  val projectDevelopers = Seq(Developer("王福强", "fujohnwang@gmail.com", blog="http://afoo.me", twitter="@fujohnwang", weibo="@囚千任"))
  val projectSite = "https://github.com/fujohnwang/afoo.publisher"
}