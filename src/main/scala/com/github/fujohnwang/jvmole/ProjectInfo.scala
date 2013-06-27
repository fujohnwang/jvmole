package com.github.fujohnwang.jvmole

case class Developer(name: String, mail: String, blog: String, twitter: String, weibo: String, remark: String = "")

trait ProjectInfo {
  val projectDevelopers = Seq(Developer("fujohnwang", "fujohnwang@gmail.com", "http://fujohnwang.github.com", "@fujohnwang", "@fujohnwang"))
  val projectSite = "https://github.com/fujohnwang/jvmole"
}