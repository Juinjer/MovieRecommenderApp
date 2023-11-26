import http from 'http';
import { Server } from 'socket.io';
import {Room} from './roomlogic.js'

const server = http.createServer();
const io = new Server(server);

const roomlist = [];
const connections = new Map();

io.on('connection', (socket) => {
  console.log('Client connected');
  socket.on('createRoom', (message) => {
    connections.set(message, socket);
    console.log(`Received: ${message}`);
    find:{
      for(const r of roomlist){
        if (message === r.host){
          socket.emit("crId",`${r.roomId}`);
          break find;
        }
      }
      let r = new Room(message);
      roomlist.push(r);
      socket.emit("crId",`${r.roomId}`);
    }
  });

  socket.on('joinRoom', (message) => {
    let args = message.split(",")
    connections.set(args[1], socket);
    find:{
      for(const r of roomlist){
        if (parseInt(args[0]) === r.roomId){
          r.addMember(args[1])
          socket.emit("jrRes",r.members.toString(),`${r.roomId}`);
          break find;
        }
      }
      socket.emit("jrRes", "404", "404")
    }
  })

  socket.on('disconnect', () => {
    console.log('Client disconnected');
  });
});

server.listen(8080, () => {
  console.log('Server listening on port 8080');
});
