# LobbyAssist
A STUN packet monitor for P2P games utilizing UDP NAT peering.
**Currently no data is persisted to disk.**
## Requirements
* [Npcap with WinPcap API-compatible mode](https://nmap.org/npcap/) or [WinPcap](https://www.winpcap.org/)
* [JRE 9](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
## Using
Use the dropdown to pick your primary IP address that users will be connecting to. Once selected packets will 
automatically be monitored. Using this dropdown you can quickly switch between interfaces to monitor. 
Move the window around by dragging with the upper left corner. When the window loses 
focus it will automatically go into compact mode, hiding controls and menu buttons.
### Flagging Users
Double click a ping to cycle between Unmarked, Liked, and Blocked. 

*Note: this is currently not saved to disk.
All flags will be lost when the application is restarted*
## Special Mentions
Inspiration from [MakeLobbiesGreatAgain by PsiLupan](https://github.com/PsiLupan/MakeLobbiesGreatAgain)