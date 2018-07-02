Hawkesbury network file generation:
link: free speed =  maximum speed to travel in the link.


NICTA network: overall 271 nodes (Evac ndoes=80, Transit nodes=184, safe nodes=5)
OSN network: overall 71... nodes and .. links


Process of expaning the NICTA network
1.extract the road network for required area of hawkesbury region.
2.rename ids so that it does not conflict with the  node/lnk ids of NICTA network
3. As the node ids, link ids and coordniates differ in OSN and NICTA networks, I could not find the NICTA link subset in the larger OSN link set. Therefore, for each Evac and Transit node (excluding the safe nodes), I calculated the node with minimum distance (the distance measures for each node is available at sub_projects/network_output/*) and created an artificial link tag with a arbitrary 1m distance. Most of the distances were less than 1m, where the highest distance was 5m. 
4. The free speed of all the artificial links (528 links), were set to infinity to minimise the unrealistic delays in travel times.
5. There are 637 links with infinity free speed

More data is avaiable at: sub_projects/network_*



