import * as http from 'http';
import { handleMovieRating } from './ratinglogic'
import { RateFilmData } from './interfaces';
import { Server as SocketIOServer, Socket } from 'socket.io';
import { Room } from './roomlogic';
import { getRandomMovies, getSimpleRecommendation, getFullRecommendation} from './api';

const server = http.createServer();
const io = new SocketIOServer(server);

const roomlist: Room[] = [];
const connections = new Map<string, Socket>();

function findRoom(roomId:number): Room|null{
    for(const r of roomlist){
        if(r.getRoomId() == roomId){
            return r;
        }
    }
    return null;
}

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

        const r:Room|null = findRoom(parseInt(args[0]));
        if(r == null){
            socket.emit("jrRes", "404", "404");
        } else{
            for (const member of r.getMembers()) {
                const soc = connections.get(member);
                soc?.emit("joinNotif", r.getNames().get(args[1]));
            }

            r.addMember(args[1]);
            console.log("members " + r.getMembers().toString() + " names " + Array.from(r.getNames()).toString());
            socket.emit("jrRes", Array.from(r.getNames().values()).toString(), `${r.getRoomId()}`);
        }
    });

	socket.on('leaveRoom', (message: string) => {
		const args = message.split(",");

		const r:Room|null = findRoom(parseInt(args[0]));
        if(r == null){
            socket.emit("jrRes", "404", "404");
        } else{
            for (const member of r.getMembers()) {
                const soc = connections.get(member);
                soc?.emit("leaveNotif", r.getNames().get(args[1]));
            }

            r.removeMember(args[1]);
            socket.disconnect();
        }
	});

	socket.on('startLobby', (message: string) => {
		console.log("started lobby", message);
		const args = message.split(",");
		const r:Room|null = findRoom(parseInt(args[0]));
        if(r == null){
            socket.emit("jrRes", "404", "404");
        } else{
			if (r.getHost() === args[1]) {
				for (const mem of r.getMembers()) {
					let sock = connections.get(mem);
					sock?.emit("hostStart");
				}
			}
        }
	});

	socket.on('getSettings', async (message: string) => {
		console.log("get settings", message)
		const roomId: number = parseInt(message);

		const r:Room|null = findRoom(roomId);
        if(r == null){
            socket.emit("jrRes", "404", "404");
        } else{
            console.log("getSettingsResp", r.getNSwipes().toString());
            socket.emit("getSettingsResp", r.getNSwipes().toString());
        }
	});

	socket.on('setSettings', async (message: string) => {
		console.log("set settings", message)
		const args = message.split(",");
		const roomId: number = parseInt(args[0]);
		const swipes: number = parseInt(args[1]);

        const r:Room|null = findRoom(roomId);
        if(r == null){
            socket.emit("jrRes", "404", "404");
        } else{
            console.log(swipes);
            r.setNSwipes(swipes);
        }
	});

    socket.on('getMovies', async (numberOfMovies: number) => {
        let movieDetails = await getRandomMovies(numberOfMovies);
        socket.emit('getMoviesResp', movieDetails);
        //const r:Room = findRoom( parseInt(message));        
        // socket.emit('getMoviesResp', await r.getNextMovies());
    });

    socket.on('rateFilm', async (data:string) =>{
        const args = data.split(",");
        const roomId: string = args[0];
        const appId: string = args[1];
        const movieTitle: string = args[2];
        const rating: number = parseInt(args[3]);

        const rateFilmData: RateFilmData = {
			roomId,
			appId,
            movieTitle,
            rating,
		};

        console.log("App id ", appId);
        handleMovieRating(rateFilmData);
    });

    socket.on('getSimilar', async(appId:string) => {
        let movies = await getSimpleRecommendation(appId);
        console.log("Similar movies", movies);
        socket.emit('getSimilarResp', movies);
    });

	socket.on('disconnect', () => {
        console.log('Client disconnected');
    });
});

server.listen(8080, () => {
	console.log('Server listening on port 8080');
});

