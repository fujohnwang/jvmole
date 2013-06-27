afoo.publisher is a site publisher with <https://github.com/fujohnwang/posts.afoo.me> as the final root content.


```bash
sbt -Dconfig.file=/Users/fujohnwang/afooworks/docworks/articles/publish.conf @/Users/fujohnwang/workspace/afoo.publisher/src/main/resources/launchconfig [markdown file]
```


**fuck, so many gotchas with sbt and its launcher things, cross-build things, etc.**





see <https://groups.google.com/forum/#!searchin/simple-build-tool/publish$20scala$20version/simple-build-tool/IhldtC4xCn8/Jy2CPQCRnHYJ>

妈的，明明是publish-local自己实现上的一个缺陷，却要使用这种手段来规避，最后确实越绕越复杂！
发布的时候使用`crossPaths := false`将artifact名称上的scala version去掉，真的是个馊主意； 
在launcher的配置文件中使用`cross-versioned: false`并强制指定artifact的scala version也只是弥补缺陷， 勉为其难

深切感受到scala cross version的痛苦！！！



<blockquote>
Anyway, I didn't make this work, although I have wonder to make it a perfect tool. I don't want to work with such a mess anymore, move to some other way! If you want to learn something about how to build command line application with sbt, maybe this project can be a sample for you.
</blockquote>
