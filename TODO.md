# TODO

1. to make the mbean management more powerful, many type converters like the ones provided by spring framework should be added so that different types can be supported via converting their string format representation to java types.

2. enable runtime loaded agents interaction
	- agent start a service as command server side proxy and accept commands by forwarding the command to agent to process
	- command client just load the agent, and interact with the agent via its service interface
	- this is a way that we can combine both sbt commands and agent functionality, otherwise, we have to process everything in the agent's implementation which may not give us a chance to use SBT commands facility.
	
3. 