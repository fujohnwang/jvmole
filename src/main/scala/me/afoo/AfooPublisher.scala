package me.afoo

import sbt._
import java.io.File
import com.typesafe.config._
import scala.sys.process._
import org.slf4j.helpers.MessageFormatter
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.{URIish, CredentialItem, CredentialsProvider}
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import java.util.concurrent.TimeUnit

final class AfooPublisher extends xsbti.AppMain with ProjectInfo {

  val configBindingKey = AttributeKey[Config]("config")

  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    MainLoop.runLogged(initialState(configuration))
  }

  def initialState(configuration: xsbti.AppConfiguration): State = {
    val commandDefinitions = publish +: config +: BasicCommands.allBasicCommands
    val commandsToRun = CONFIG +: "iflast shell" +: configuration.arguments.map(_.trim)
    State(configuration, commandDefinitions, Set.empty, None, commandsToRun, State.newHistory, AttributeMap.empty, initialGlobalLogging, State.Continue)
  }

  def initialGlobalLogging: GlobalLogging = {
    var logFile = new File("afoo-publisher.log")
    if (!logFile.exists()) {
      if (!logFile.createNewFile()) logFile = File.createTempFile("afoo-publisher", "log")
    }
    GlobalLogging.initial(MainLogging.globalDefault _, logFile, ConsoleLogger.systemOut)
  }

  val CONFIG = "config"
  val config = Command.command(CONFIG) {
    s => val config = ConfigFactory.load(); s.put(configBindingKey, config)
  }

  val PUBLISH = "publish"
  val publish = Command.single(PUBLISH) {
    (s, source) => s.get(configBindingKey) match {
      case Some(config) => {
        val sourceFile = new File(config.getString("posts.source.dir"), source)
        if (!sourceFile.exists()) {
          s.log.error(s"the post file:$sourceFile doesn't exist")
          s.fail
        } else {
          val (out, err) = (new StringBuilder, new StringBuilder)
          val processLogger = ProcessLogger(o => out.append(o), e => err.append(e))
          val targetFile = new File(config.getString("posts.local.target.dir"), source.substring(0, source.lastIndexOf(".")) + ".html")
          if (MessageFormatter.arrayFormat("pandoc -s -N --toc --template={} {} -o {}", Array(config.getString("pandoc.transform.html.template"), sourceFile.getAbsolutePath, targetFile.getAbsolutePath)).getMessage ! processLogger == 0) {
            s.log.info(s"transform $sourceFile to $targetFile with pandoc successfully.")

            val git = Git.open(targetFile.getParentFile)
            git.add().addFilepattern(".").call()
            git.commit().setMessage(s"publish new post $targetFile").call()
            git.push().setCredentialsProvider(new PassphraseCredentialsProvider(config.getString("github.passphrase"))).setRemote("origin").call()
            s.log.info("git remote push is done.")

            synchronizeWebServer(s, config)
            s.log.info("synchronize web server root directory is done.")
            s.log.info(s"Post $targetFile is published successfully")
            s
          } else {
            s.log.error(s"failed to transform the sourceFile:$sourceFile with error:${err.toString}")
            s.fail
          }
        }
      }
      case None => s.log.error("no config binding found, run `config` command first"); s.fail
    }
  }


  private def synchronizeWebServer(s:State, config:Config) {
    val webServerAddress = config.getString("web.server.address")
    val webServerRootDir = config.getString("web.server.root.dir")
    val username = config.getString("web.server.ssh.username")
    val password = config.getString("web.server.ssh.password")
    val commandExecTimeout = config.getInt("web.server.ssh.command.timeout")

    val ssh: SSHClient = new SSHClient
    ssh.loadKnownHosts
    ssh.connect(webServerAddress)
    try {
      ssh.authPassword(username, password)
      val session = ssh.startSession()
      try {
        val cmd = session.exec(s"cd $webServerRootDir; git pull")
        s.log.info(IOUtils.readFully(cmd.getInputStream()).toString());
        cmd.join(commandExecTimeout, TimeUnit.SECONDS);
        s.log.info("\n** exit status: " + cmd.getExitStatus());
      } finally {
        session.close()
      }
    } finally {
      ssh.disconnect
    }
  }

}


class PassphraseCredentialsProvider(passphrase: String) extends CredentialsProvider {
  def isInteractive: Boolean = true

  def supports(items: CredentialItem*): Boolean = true

  def get(uri: URIish, items: CredentialItem*): Boolean = {
    setPassphrase(items: _*)
    true
  }

  protected def setPassphrase(items: CredentialItem*) {
    for (item <- items) {
      if (item.isInstanceOf[CredentialItem.StringType]) {
        item.asInstanceOf[CredentialItem.StringType].setValue("Zero1one")
        return
      }
    }
  }
}
