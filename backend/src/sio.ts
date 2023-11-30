import * as http from 'http';
import { Server as SocketIOServer, Socket } from 'socket.io';
import { Room } from './roomlogic';
import { randomMovie } from './api';

const server = http.createServer();
const io = new SocketIOServer(server);

const roomlist: Room[] = [];
const connections = new Map<string, Socket>();

io.on('connection', (socket: Socket) => {
	console.log('Client connected');

	socket.on('createRoom', (message: string) => {
		connections.set(message, socket);
		console.log(`Received: ${message}`);

		find: {
			for (const r of roomlist) {
				if (message === r.getHost()) {
					socket.emit("crId", `${r.getRoomId()}`);
					break find;
				}
			}

			const r = new Room(message);
			roomlist.push(r);
			socket.emit("crId", `${r.getRoomId()}`);
		}
	});

	socket.on('joinRoom', (message: string) => {
		const args = message.split(",");
		connections.set(args[1], socket);

		find: {
			for (const r of roomlist) {
				if (parseInt(args[0]) === r.getRoomId()) {
					for (const member of r.getMembers()) {
						const soc = connections.get(member);
						soc?.emit("joinNotif", r.getNames().get(args[1]));
					}

					r.addMember(args[1]);
					console.log("members " + r.getMembers().toString() + " names " + Array.from(r.getNames()).toString());
					socket.emit("jrRes", Array.from(r.getNames().values()).toString(), `${r.getRoomId()}`);
					break find;
				}
			}

			socket.emit("jrRes", "404", "404");
		}
	});

	socket.on('leaveRoom', (message: string) => {
		const args = message.split(",");

		find: {
			for (const r of roomlist) {
				if (parseInt(args[0]) === r.getRoomId()) {
					for (const member of r.getMembers()) {
						const soc = connections.get(member);
						soc?.emit("leaveNotif", r.getNames().get(args[1]));
					}

					r.removeMember(args[1]);
					socket.disconnect();
					break find;
				}
			}
		}
	});

	socket.on('startLobby', (message: string) => {
		console.log("started lobby", message)
		const args = message.split(",");
		for (const r of roomlist) {
			if (r.getRoomId() === parseInt(args[0]) && r.getHost() === args[1]) {
				for (const mem of r.getMembers()) {
					let sock = connections.get(mem);
					sock?.emit("hostStart");
				}
			}
		}
	});

	socket.on('getMovie', async (message: String) => {
		let movieDetails = await randomMovie()
		socket.emit('getMovieResp', movieDetails.img,movieDetails.title,movieDetails.desc)
	});

	socket.on('disconnect', () => {
		console.log('Client disconnected');
	});
});

server.listen(8080, () => {
	console.log('Server listening on port 8080');
});
