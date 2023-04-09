# learn_network_socket_jvav

## Introduction

A tiny program for learning Socket and Swing.

In this project, I use ChatGPT to learn the usage of Swing, and I discuss the solution of some exceptions with him.

It's interesting to debug the program with ChatGPT. There was a problem where the client would not close automatically after the server closed. He gave lots of advice which, however, didn't solve the problem. Nevertheless, I was inspired and put the input stream and output stream objects into global variables, which successfully fixed the issue.

## TODO

- [x] Connect and Disconnect
- [x] Send message from client
- [x] Broadcast message from server
- [x] File sending and receiving
- [ ] Server: Send message to specific client
- [ ] Server: Client management