# Elrond Prototype
State Sharding PoC for Elrond network

# Introduction
This readme will give some insight into the code, architecture and several
concepts used in this prototype.

# Table of Contents

-   [Design goal](#design-goal)
    - [What to expect](#what-to-expect)
    - [What not to expect](#what-not-to-expect)
-   [Directory structure](#directory-structure)
-   [Components](#components)
    - [P2P communication](**P2P-communication**)
    - [Cryptography](**cryptography**)
    - [Chronology](**chronology**)
    - [Consensus](**consensus**)
    - [Data layer](**data-layer**)
    - [Sharding](**Sharding**)
    - [Execution layer](**execution-layer**)
-   [How to run a node](#how-to-run-a-node)



## Design goal
Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s

### What to expect
This repository was originally created and used to validate several
assumptions related to state sharding and cross shard transactions.
This can be considered as a sandbox to test various mechanisms as
preparation for developing the testnet.

### What not to expect
This is not a testnet so expect not all features or functionalities to
be available. As our main focus was on validating assumptions regarding
sharding and not improvements on performance, there are things that could
have been optimised. These will be considered while developing Elrond testnet.

## Directory structure
The code is split into two sections:
- **/elrond-api** - implementing a web application and the REST APIs allowing elrond
client to interact with a node (computer running the Elrond Prototype code)
- **/elrond-core** - implements the core part of the Elrond Prototype protocol

## Components
- **P2P communication**: the prototype uses [tomp2p](https://tomp2p.net/) for
its p2p networking model and communication primitives. Communication is done
on channels for the registered topics: Transactions, Blocks, Receipts,
Cross Shard transactions, etc. Message relay across channels is currently done by direct
send to the registered peers, which is really bad in terms of performance
and memory consumption but easy to implement in first phase. This needs to
be abstracted on a gossip model at shard level.
- **Cryptography**: the prototype uses cryptographic primitives from
[spongycastle](https://github.com/rtyley/spongycastle) and implements Schnorr
signature scheme for validating transactions and [Belare-Neven multisignature
scheme](https://cseweb.ucsd.edu/~mihir/papers/multisignatures-ccs.pdf) for
signing and validating block signatures. The multisignature scheme allows
for signature aggregation, so no matter how many signers participate in
signing a single block the resulting signature is always 64 bytes long.
- **Chronology**: time is split into epochs, and each epoch is split into
rounds. In each round there is a new consensus group created, formed by a
block proposer/leader and validators. Each round is again split into subrounds
mapped to the consensus phases. Elrond Prototype focuses more on what happens
during rounds and subrounds, so the epoch actions are not yet implemented.
- **Consensus**: the consensus mechanism used in the prototype is a round robin
mechanism where each node takes turns in proposing blocks. Although the random
sampling of proposer and validators is already implemented in a separate branch,
changes to the PBFT consensus still have to be operated. The switch to PBFT consensus and selection
of validators will be done as soon as the branch is ready - **put on hold as
decided to switch to *Go* for the testnet. PBFT Consensus will be first implemented
in testnet repository**
- **Data layer**: main data models implemented: Block, Receipt, Transaction, Account,
Trie etc. For serialization we are currently using either json or Ethereum's RLP.
Probably we will switch to either protobufs or capn'proto in testnet for the performance
boost, but this is not relevant in the prototype. For persistent storage we are currently using LevelDB
while for non-persistent storage LRU maps or Guava Cache.
- **Sharding**: Phase 1 - we are using a static sharding model where we
need to define an initial number of shards. State is currently sharded, each
shard maintains only accounts associated to its shard and the corresponding blockchain.
For the time being nodes will be placed in shards according to their PK using the
same allocation mechanism as for dispatching transactions - will be changed when implementing
epoch events. Notarization chain is not yet implemented - relaying cross shard transactions
is done through messaging in cross shard communication channels. This will no longer be needed
after the introduction of the notarization chain, as our dispatching of transactions takes care
of the availability in destination shard, and the inclusion proofs take care of
validation (Merkle proofs will be replaced with accumulators)
- **Execution layer**: Includes several executors: bootstrapping, synchronization and chronology tasks
(proposals and validations) + transaction and block execution, account state rollback.
Interceptors: blocks, receipts, transactions, cross shard transactions.

## How to run a node
You can run an Elrond Prototype node either through Swagger, or by using the
[elrond-ui](https://github.com/ElrondNetwork/elrond-ui)
For this you need first to run the elrond-api jar.

![elrond-core-PoC1](https://i.imgur.com/SrFoOqu.png)

To build the jar you should go to root folder and run
```
$ mvn package
```

This will generate a jar  ```/elrond-api/target/elrond-api-1.0-SNAPSHOT.jar``` that you can run
then with:
```
java -jar ./elrond-api/target/elrond-api-1.0-SNAPSHOT.jar
```

Wait until you you get a message like

```
Started ElrondApiApplication in 15.48 seconds (JVM running for 17.733)
```

Now you should be ready to either launch the elrond-ui or start using the swagger.
To use swagger open a browser page and go to : http://localhost:8080/swagger-ui.html

In elrond-node-controller you can find all currently available REST APIs.

To start the node you should use the ```/node/start``` REST API

**Take care about the port and Ip** you are using. The first instance you start should be the master node, so
port and master peer port should be the same, and ip address should be the ip of the machine you are running
on, or localhost. The subsequent nodes should use for the masterPeerPort and masterPeerIpAddress
the ones from the first node. If you are trying to run multiple node instances on the same machine,
you can start the elrond-api for other instances but you need to change the port (default is 8080)
```
java -jar ./elrond-api/target/elrond-api-1.0-SNAPSHOT.jar --server.port=8081
```

Now you can start swagger on the new port to configure and start the new elrond prototype node instance
http://localhost:8081/swagger-ui.html

One difference between a master node and a peer node (not master node) instance is that the master receives
in its account the entire balance in the system, all other nodes start up with 0 balance.
There are some other functionalities working only on the first instance, such as benchmarking
all shards. This is also the reason why each new connecting node should connect to the network
through this master node. If benchmarking shards is not required, then subsequent nodes can
connect to the network through any other already connected node.
