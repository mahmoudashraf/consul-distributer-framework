# consul-leaderelection
 Leader Election Module for services running across Concul

Building distributed appliactions became easrier with spring-consul 
consul Leader elction add a segnificats advantagous for spring consul projects 
to allow developers creating distributed apps running as services manged vy consul (registeration-config)

How It Works:
Leader elction run in background to observe any change in cluster leader info
acording to changes consul-leader-election will get new leader information or volounteer with a current service running with consul leader elcetion utulity to become a new leader 

All in background without any intervention from service running  


Consul-Leader-Election  
provide an interface  to receive notifications from Leader observer to act as leader if granted or act as servent 

privides annoutation based (OnLeader,OnServent) to control access to specific methods to run only on leader mode or servent mode and it throws exceptions relevent and informative (UnsupportedOperationOnLeaderMode,UnsupportedOperationOnServentMode.java).








