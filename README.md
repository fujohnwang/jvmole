JVM Mole, guess what it does ;-)

# What it can do

当前来说，jvmole支持的功能很简单，运行jvmole进入交互命令行console之后， 执行help命令即可知道其基本功能：

	[info] welcome to jvmole's world~
	> help

	  beans [pattern]                                  list mbeans registered
	  exec <mbean> <method name> <method args>*        invoke mbean method
	  desc <mbean>                                     describe mbean
	  set <mbean> <attribute name> <attribute value>   set attribute of some mbean
	  lsvm                                             list running virtual machine instances that we can attach to
	  attach <pid>                                     attach to specific virtual machine instance as per pid
	  detach                                           detach from attached JVM instance if any
	  help                                             Displays this help message or prints detailed help on requested commands (run 'help <command>').
	  ; <command> (; <command>)*                       Runs the provided semicolon-separated commands.
	  exit                                             Terminates the build.
	  ~ <command>                                      Executes the specified command whenever source files change.

	More command help available using 'help <command>' for:
	  !, -, <, alias, append, apply, iflast, reboot, shell

	> 

简单来讲，就是可以执行`lsvm`命令获取可以`attach`到哪个JVM进程上去，或者`detach`下来；
如果`attach`成功，则可以执行`beans <pattern>`命令来查询有哪些JMX MBeans， 然后`desc`或者`set`(attribute)或者`exec`(method)，仅此而已。

谁让我只想练练手，做个prototype那，没想做的很复杂且功能完备。

不过如果某位看官感兴趣，倒是可以fork并在此基础上做扩展哦， 窥探JVM进程状态， 加载某些java agent做class instrumentation之类，都OK的啦，没有做不到，只有想不到，哈哈， GL&HF

# How to use 

* 如果使用conscript安装，那么恭喜你，在本地直接执行`jvmole`即可， 因为conscript已经为你配置好了环境以及__jvmole__命令；

* 如果从源码编译安装，那么在jvmole目录下，执行`sbt @src/main/conscript/jvmole/launchconfig`也可以（如果想在任何位置执行，则将src/main/conscript/jvmole/launchconfig的路径改为绝对路径）。

	NOTE： 注意sbt命令后面，launchconfig文件路径开始的@符号， it's a must! 原因嘛，参考sbt launcher规范 ：-）


# How to Install
两种方式：

1. 直接使用[conscript](https://github.com/n8han/conscript)安装：
	- 安装conscript
	- `cs fujohnwang/jvmole`
2. 源码编译安装：
	- `git clone git://github.com/fujohnwang/jvmole.git`
	- `cd jvmole`
	- `sbt publish-local`

# Why I wrote this

just for fun, and learn how to write a serious interactive commandline application with sbt's tab-completion commands support.