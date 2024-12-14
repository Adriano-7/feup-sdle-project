# Server Architecture

![architecture](https://hackmd.io/_uploads/B1fDGlnf1g.png)

This diagram represents the server architecture used to manage the data and guarantee high availability and consistency for the application. The solution was designed with a focus on replication, load balancing and efficient conflict management.

## System components


### 1. Replicated servers (Server1, Server2, Server3)
The servers represent the distributed nodes responsible for storing the shopping lists redundantly.
Each server maintains a local database for efficient storage and acts as part of the system's replica.
Replication is used to ensure that data is accessible even if one or more servers fail.

### 2. Load Balancer
The Load Balancer acts as the central intermediary that manages communication between servers and clients.
Its main functions include:
- Data Distribution: Determines which server the data should be stored on, based on a consistent hashing mechanism.
- Replication: Manages the replication of shopping lists to predecessor servers.
- Centralized Control: Serves as a single point of reference for the architecture, controlling load balancing and the integration of new servers.

## Hash Ring

The system's architecture uses a Consistent Hashing mechanism to manage the distribution of shopping lists between servers.

### Server distribution:

- Each server is represented by a unique identifier (generated by applying one of the hashing algorithms to the server's IP).
- These identifiers are mapped as a circular space, forming the hash ring.

### Data allocation:

- Each list created by the client is given a unique identifier (ID), which is also mapped to the same circular space.
- A list is assigned to the server whose identifier is the first to appear clockwise in the ring, after the list ID.

### Range of Responsibility:

- Each server is responsible for storing all lists whose IDs are in the range between the identifier of its predecessor in the ring and its own identifier.

Here's an example to explain how the Hash Ring works with three servers, that have random values:

![hash_ring](https://hackmd.io/_uploads/HyStYg2Gkg.png)