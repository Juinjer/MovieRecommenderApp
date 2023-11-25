import express from 'express';
import {Room} from './roomlogic.js'

export const router = express.Router();

const roomlist = [];

router.use((req, res, next) =>{
    console.log("connection: " + req.headers["host"]);
    console.log(req["originalUrl"]);
    next();
})

router.get("/createRoom", (req,res)=> {
    const phoneId = req.query.id;
    for (let i = 0; i < roomlist.length; i++) {
        if (roomlist[i].host==req.query.id){
            res.send(`${roomlist[i].roomId}`)
            return;
        }
    }
    let room = new Room(phoneId);
    roomlist.push(room);
    res.send(`${room.roomId}`);
});

router.get("/hostleaveRoom", (req,res) => {
    console.log("leave")
    for (let i = 0; i < roomlist.length; i++) {
        if (roomlist[i].host==req.query.id){
            roomlist.splice(i,1);
            break;
        }
    } 
});

router.get("/joinRoom", (req,res) => {
    find:{
        for(const room of roomlist){
            if (room.roomId==req.query.roomid){
                room.addMember(req.query.id);
                res.send(room.members.toString());
                console.log(room.members.toString());
                break find;
            }
        }
        res.sendStatus(404)
    }
})

