import * as http from 'http';
import { Movie, MovieRating } from './interfaces';
import { Server as SocketIOServer, Socket } from 'socket.io';
import { Room } from './roomlogic';

const server = http.createServer();
const io = new SocketIOServer(server);

const roomList: Room[] = [];
const connections = new Map<string, Socket>();
function removeRoom(roomId:number) {
    for(let i = 0; i<roomList.length;i++){
        if(roomList[i].getRoomId() == roomId){
            roomList.splice(i,i);
        }
    }
}

function findRoom(roomId:number): Room|null{
    for(const room of roomList){
        if(room.getRoomId() == roomId){
            return room;
        }
    }
    return null;
}

export function notifyProcessingDone(roomMembers: string[], suggestions:Movie[]) {
    if (roomMembers != null) {
        const suggestionsJSON = JSON.stringify(suggestions);
        // Iterate through the members of the room and retrieve their sockets from the connections map
        for (const member of roomMembers) {
            const socket = connections.get(member)!!;
            socket.emit('processingDone', suggestionsJSON);
        }
    } else {
        console.error("error in emitting to room members");
    }
}

io.on('connection', (socket: Socket) => {
	console.log('Client connected');

	socket.on('createRoom', (message: string) => {
		connections.set(message, socket);
		console.log(`Received: ${message}`);

		find: {
			for (const room of roomList) {
				if (message === room.getHost()) {
					socket.emit("crId", `${room.getRoomId()}`);
					break find;
				}
			}

			const room = new Room(message);
			roomList.push(room);
			socket.emit("crId", `${room.getRoomId()}`);
		}
	});

	socket.on('joinRoom', (message: string) => {
		const args = message.split(",");
		connections.set(args[1], socket);
        console.log(`joinroom, ${message}`)

        const room:Room|null = findRoom(parseInt(args[0]));
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else if (room.getStatus() === true) {
            socket.emit("jrRes", "999", "999");
        } else{
            room.addMember(args[1]);
            for (const member of room.getMembers()) {
                const soc = connections.get(member);
                soc?.emit("joinNotif", room.getNames().get(args[1]));
            }

            socket.emit("jrRes", Array.from(room.getNames().values()).toString(), `${room.getRoomId()}`);
        }
    });

	socket.on('leaveRoom', (message: string) => {
		const args = message.split(",");

        console.log("leaveRoom received");
		const room:Room|null = findRoom(parseInt(args[0]));
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else{
            for (const member of room.getMembers()) {
                const soc = connections.get(member);
                if (args[1]===room.getHost()) {
                    soc?.emit("disbandgroup");
                } else {
                    soc?.emit("leaveNotif", room.getNames().get(args[1]));
                }
            }
            if (args[1]===room.getHost()) {
                removeRoom(parseInt(args[0]));
            } else {
                room.removeMember(args[1]);
            }
            socket.disconnect();
        }
	});

	socket.on('startLobby', (message: string) => {
		console.log("started lobby", message);
		const args = message.split(",");
		const room:Room|null = findRoom(parseInt(args[0]));
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else{
			if (room.getHost() === args[1]) {
                room.start();
				for (const mem of room.getMembers()) {
					let sock = connections.get(mem);
                    console.log("testMember")
					sock?.emit("hostStart");
				}
			}
        }
	});

	socket.on('getSettings', async (message: string) => {
		console.log("get settings", message)
		const roomId: number = parseInt(message);

		const room:Room|null = findRoom(roomId);
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else{
            //console.log("getSettingsResp", room.getNSwipes().toString());
            socket.emit("getSettingsResp", room.getNSwipes().toString());
        }
	});

	socket.on('setSettings', async (message: string) => {
		console.log("set settings", message)
		const args = message.split(",");
		const roomId: number = parseInt(args[0]);
		const swipes: number = parseInt(args[1]);

        const room:Room|null = findRoom(roomId);
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else{
            //console.log(swipes);
            room.setNSwipes(swipes);
        }
	});

    /*
    socket.on('getMovies', async (numberOfMovies: number) => {
        let movieDetails = await getSuggestionsRandom(numberOfMovies);
        console.log("testMovieResp");
        console.log(`${movieDetails}`);
        socket.emit('getMoviesResp', movieDetails);
        //const room:Room = findRoom( parseInt(message));        
        // socket.emit('getMoviesResp', await room.getNextMovies());
    });
    */

    socket.on('rateFilm', async (data: string) => {
        try {
            const args = data.split(";;;");
            const roomId: number = parseInt(args[0]);
            const appId: string = args[1];
            const movieJsonString: string = args[2];
            const rating: number = parseInt(args[3]);

            const movie: Movie = JSON.parse(movieJsonString);

            const movieRating: MovieRating = {
                appId,
                rating,
            };

            const room:Room|null = findRoom(roomId);
            if ( room == null) {
                socket.emit("jrRes", "404", "404");
            } else {
                room.addMovieRating(movie, movieRating);
                console.log("Rating added", `{${movie.title}}`, movieRating);
            }
        } catch (error) {
            console.error('Error processing rateFilm event:', error);
        }
    });

    //TODO room api for the processing?
    socket.on('getSuggestions', async(roomId: number) => {
        console.log("Suggestions requested", roomId);
        const room:Room|null = findRoom(roomId);
        if(room == null){
            socket.emit("jrRes", "404", "404");
        } else{
            let suggestions = await room.getSuggestions();
            socket.emit('getSuggestionsResp', JSON.stringify(suggestions));
        }
    });

/*
    socket.on('getSimilar', async(movieTitle:string) => {
        let movies = await getFullRecommendation(movieTitle);
        console.log("Similar movies", movies);
        socket.emit('getSimilarResp', movies);
    });
*/
	socket.on('disconnect', () => {
        console.log('Client disconnected');
    });

    async function sendRecommendations(recommendations: Movie[]) {
            socket.emit('recommendations', recommendations);
    }

});
let port = 9999;
server.listen(port, () => {
	console.log('Server listening on port 9999');
});

