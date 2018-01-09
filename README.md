# Distributed File System
A distributed, replicated, and fault tolerant file system
## Details
### Components
* chunk server is for managing file chunks
* controller node is for managing information about chunk servers and chunks within the system
* client is for storing, retrieving, and updating files in the system
### Optimization
* designed an algorithm to balance the load among all the nodes
* utilized heartbeat mechanism to check all the replicas have the same data and detect failures

