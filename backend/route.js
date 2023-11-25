import express from 'express';
// import * as roomlogic from 'roomlogic.js'

export const router = express.Router();

router.use((req, res, next) =>{
    console.log("connection: " + req.headers["host"])
    next()
})

router.get("/createRoom", (req,res)=> {
    let min = 100000;
    let max = 999999;
    let number = Math.floor(Math.random() * (max - min) + min);
    console.log(`${number}`)
    res.send(`${number}`);
});

